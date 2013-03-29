
package org.teameos.settings.device;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class PhoneService extends Service {
    static String TAG = "PhoneService";

    // reflection, may move later
    //
    // Phone
    private Method invokeOemRilRequestRaw = null;
    private Method setRadioPower = null;
    private Method getCdmaPrlVersion = null;
    private Object mPhone = null;
    // Asyncresult
    private Class<?> AsyncResult = null;
    private Field userObj = null;
    private Field exception = null;
    private Field result = null;

    private Context mContext;
    private final IncomingHandler mHandler = new IncomingHandler();
    private final Messenger mMessenger = new Messenger(mHandler);
    private ArrayList<Messenger> mClients = new ArrayList<Messenger>();
    private static boolean isRunning = false;
    private int main_cmd_hidden;
    private int device_short;
    private boolean isTuna = false;

    // PRL Writer and ril constants
    private static final int MAX_PRL_SIZE = 16384;
    private static final int OEM_FUNCTION_ID_OMADM = 10;
    private static final int OEM_OMADM_WRITE_PRL = 28;
    private static final int OMADM_PRL_RESP = 9999980;
    private int bytesWritten;
    private byte dstbyteArray[];
    private int fileSize;
    private FileInputStream in;
    private int prl_length;
    private ByteBuffer prlbytedata;

    // MSL ril constants
    private static final int GET_MSL_DONE = 1998;
    private static final int OEM_SUB_CMD_GET_MSL = 2;
    private static final int OEM_SUB_RAW_MSL = 1;
    private static final int TUNA_WRITE_SHORT = 5;
    private static final int CRESPO_WRITE_SHORT = 4;

    // handler messages
    public static final int MSG_REGISTER_CLIENT = 1;
    public static final int MSG_UNREGISTER_CLIENT = 2;
    public static final int MSG_SET_INT_VALUE = 3;
    public static final int MSG_SET_STRING_VALUE = 4;
    public static final int REQUEST_START_PRL = 10001;
    public static final int REQUEST_GET_MSL = 10002;
    public static final int REQUEST_UPDATE_PRL = 10003;
    public static final int NOTIFY_WRITE_PRL_DONE = 10004;
    public static final int NOTIFY_GET_MSL = 10005;
    public static final int NOTIFY_WRITE_PRL_UPDATE = 10006;
    public static final int NOTIFY_WRITE_PRL_FAILED = 10007;
    public static final int NOTIFY_GET_MSL_FAILED = 10008;
    public static final int NOTIFY_UPDATE_PRL_NUMBER = 10009;
    public static final int NOTIFY_WRITE_PRL_START = 10010;
    public static final int NOTIFY_WRITE_PRL_RADIO_ON = 10011;
    public static final int NOTIFY_WRITE_PRL_RADIO_RESTARTING = 10012;

    // messenger object keys
    public static final String REQUEST_PRL_PATH_KEY = "notify_prl_path_key";
    public static final String NOTIFY_WRITE_PRL_UPDATE_KEY = "notify_write_prl_update_key";
    public static final String NOTIFY_PRL_VALUE_KEY = "notify_prl_value_key";
    public static final String NOTIFY_MSL_VALUE_KEY = "notify_msl_value_key";

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        log("Service bound to activity " + intent.toString());
        return mMessenger.getBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        log("Service Destroyed");
        isRunning = false;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        log("Service created");
        isRunning = true;
        mContext = getApplicationContext();
        main_cmd_hidden = InitHelper.getOemMainCommandHidden(mContext);
        if (InitHelper.isCrespo(mContext)) {
            device_short = CRESPO_WRITE_SHORT;
        } else if (InitHelper.isTuna(mContext)) {
            device_short = TUNA_WRITE_SHORT;
            isTuna = true;
        }
        initPhoneHelper();
    }

    public static boolean isRunning() {
        return isRunning;
    }

    private void sendMessageToUI(int what, String key, String value) {
        for (int i = mClients.size() - 1; i >= 0; i--) {
            try {
                Bundle b = new Bundle();
                b.putString(key, value);
                Message msg = Message.obtain(null, what);
                msg.setData(b);
                mClients.get(i).send(msg);
            } catch (RemoteException e) {
                mClients.remove(i);
            }
        }
    }

    private void sendEmptyMessageToUI(int what) {
        for (int i = mClients.size() - 1; i >= 0; i--) {
            try {
                Message msg = Message.obtain(null, what);
                mClients.get(i).send(msg);
            } catch (RemoteException e) {
                mClients.remove(i);
            }
        }
    }

    private void sendIntMessageToUI(int what, String key, int value) {
        for (int i = mClients.size() - 1; i >= 0; i--) {
            try {
                Bundle b = new Bundle();
                b.putInt(key, value);
                Message msg = Message.obtain(null, what);
                msg.setData(b);
                mClients.get(i).send(msg);
            } catch (RemoteException e) {
                mClients.remove(i);
            }
        }
    }

    class IncomingHandler extends Handler {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle b = msg.getData();
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    mClients.add(msg.replyTo);
                    sendEmptyMessageToUI(MSG_REGISTER_CLIENT);
                    break;
                case MSG_UNREGISTER_CLIENT:
                    sendEmptyMessageToUI(MSG_UNREGISTER_CLIENT);
                    mClients.remove(msg.replyTo);
                    break;
                case REQUEST_GET_MSL:
                    log("Received MSL request from ui");
                    checkMSLCode();
                    break;
                case GET_MSL_DONE:
                    log("MSL response incoming from ril");
                    Object asyncresult1 = AsyncResult.cast(msg.obj);
                    try {
                        if (exception.get(asyncresult1) != null) {
                            log("AsyncResult Exception Occured!");
                            sendEmptyMessageToUI(NOTIFY_GET_MSL_FAILED);
                        } else if (result.get(asyncresult1) == null) {
                            log("ar.result == NULL - No answer for MSL response");
                            sendEmptyMessageToUI(NOTIFY_GET_MSL_FAILED);
                        } else {
                            byte abyte0[] = (byte[]) (byte[]) result
                                    .get(asyncresult1);
                            String s = new String("");
                            for (int j = 0; j < 6; j++)
                                s = (new StringBuilder()).append(s)
                                        .append((char) abyte0[j]).toString();
                            log("Acquired MSL Code: " + s);
                            sendMessageToUI(NOTIFY_GET_MSL, NOTIFY_MSL_VALUE_KEY, s);
                        }
                    } catch (Exception e) {
                        sendEmptyMessageToUI(NOTIFY_GET_MSL_FAILED);
                        e.printStackTrace();
                    }
                case REQUEST_UPDATE_PRL:
                    sendPrlUpdate();
                    break;
                case REQUEST_START_PRL:
                    String apath = b.getString(REQUEST_PRL_PATH_KEY);
                    if (apath != null) {
                        File path = new File(apath);
                        if (path.exists()) {
                            writePrlToRadio(path);
                            sendEmptyMessageToUI(NOTIFY_WRITE_PRL_START);
                        } else {
                            sendEmptyMessageToUI(NOTIFY_WRITE_PRL_FAILED);
                        }
                    } else {
                        sendEmptyMessageToUI(NOTIFY_WRITE_PRL_FAILED);
                    }
                    break;
                case OMADM_PRL_RESP:
                    Object asyncresult;
                    log((new StringBuilder())
                            .append("OMADM PRL IPC response received : ")
                            .append(msg).toString());
                    asyncresult = AsyncResult.cast(msg.obj);
                    if (asyncresult != null) {
                        try {
                            if (exception.get(asyncresult) != null)
                                log((new StringBuilder())
                                        .append("AsyncResult Exception for PRL IPC : ")
                                        .append(exception.get(asyncresult))
                                        .toString());
                            if (result.get(asyncresult) == null)
                                log((new StringBuilder())
                                        .append("No answer for PRL IPC")
                                        .append(asyncresult).toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                            sendEmptyMessageToUI(NOTIFY_WRITE_PRL_FAILED);
                        }
                    } else {
                        log("No AsyncResult for PRL IPC");
                        sendEmptyMessageToUI(NOTIFY_WRITE_PRL_FAILED);
                    }

                    log((new StringBuilder()).append("bytesWritten : ")
                            .append(bytesWritten).toString());
                    if (bytesWritten < prl_length) {
                        int i = prl_length - bytesWritten;
                        int j;
                        byte abyte0[];
                        ByteArrayOutputStream bytearrayoutputstream;
                        DataOutputStream dataoutputstream;
                        if (i > 200) {
                            i = 200;
                            j = 1;
                        } else {
                            j = 0;
                        }
                        abyte0 = new byte[i];
                        prlbytedata.get(abyte0, 0, abyte0.length);
                        bytearrayoutputstream = new ByteArrayOutputStream();
                        dataoutputstream = new DataOutputStream(
                                bytearrayoutputstream);
                        try {
                            fileSize = 8 + abyte0.length;
                            log((new StringBuilder()).append("bytesWritten : ")
                                    .append(bytesWritten)
                                    .append(" / dstArray.length : ")
                                    .append(abyte0.length).append(" / more : ")
                                    .append(j).append(" /filesize : ")
                                    .append(fileSize).toString());
                            dataoutputstream.writeByte(OEM_FUNCTION_ID_OMADM);
                            dataoutputstream.writeByte(OEM_OMADM_WRITE_PRL);
                            dataoutputstream.writeShort(fileSize);
                            dataoutputstream.writeShort(prl_length);
                            dataoutputstream.writeByte(abyte0.length);
                            dataoutputstream.writeByte(j);
                            dataoutputstream.write(abyte0);
                            invokeOemRilRequestRaw(
                                    bytearrayoutputstream.toByteArray(),
                                    mHandler.obtainMessage(OMADM_PRL_RESP));
                            bytesWritten = bytesWritten + abyte0.length;
                            sendIntMessageToUI(NOTIFY_WRITE_PRL_UPDATE,
                                    NOTIFY_WRITE_PRL_UPDATE_KEY, bytesWritten);

                            if (bytesWritten == prl_length) {
                                sendEmptyMessageToUI(NOTIFY_WRITE_PRL_DONE);
                                setRadioPower(false);
                                setRadioPower(true);
                                sendDelayedPrlUpdate();
                            }
                        } catch (FileNotFoundException filenotfoundexception) {
                            filenotfoundexception.printStackTrace();
                            sendEmptyMessageToUI(NOTIFY_WRITE_PRL_FAILED);
                        } catch (IOException ioexception) {
                            ioexception.printStackTrace();
                            sendEmptyMessageToUI(NOTIFY_WRITE_PRL_FAILED);
                        }
                    }
                default:
                    super.handleMessage(msg);
            }
        };
    };

    private void sendDelayedPrlUpdate() {
        // a little hackish here but its very difficult to determine
        // exactly when the prl updates after radio is back on
        // so we'll try 10 seconds
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                String prl = getCdmaPrlVersion();
                sendMessageToUI(NOTIFY_UPDATE_PRL_NUMBER, NOTIFY_PRL_VALUE_KEY,
                        prl);                
            }            
        }, 10 * 1000);
    }

    private void sendPrlUpdate() {
        String prl = getCdmaPrlVersion();
        sendMessageToUI(NOTIFY_UPDATE_PRL_NUMBER, NOTIFY_PRL_VALUE_KEY,
                prl);
    }

    private void checkMSLCode() {
        ByteArrayOutputStream bytearrayoutputstream;
        DataOutputStream dataoutputstream;
        bytearrayoutputstream = new ByteArrayOutputStream();
        dataoutputstream = new DataOutputStream(bytearrayoutputstream);
        try {
            dataoutputstream.writeByte(main_cmd_hidden);
            dataoutputstream.writeByte(OEM_SUB_CMD_GET_MSL);
            dataoutputstream.writeShort(device_short);
            if (InitHelper.isTuna(mContext)) {
                dataoutputstream.writeByte(OEM_SUB_RAW_MSL);
            }
        } catch (IOException e) {
            e.printStackTrace();
            sendEmptyMessageToUI(NOTIFY_GET_MSL_FAILED);

        }
        invokeOemRilRequestRaw(bytearrayoutputstream.toByteArray(),
                mHandler.obtainMessage(GET_MSL_DONE));
    }

    private void writePrlToRadio(File prl) {
        try {
            in = new FileInputStream(prl);

            prlbytedata = ByteBuffer.allocate(MAX_PRL_SIZE);
            dstbyteArray = prlbytedata.array();
            prl_length = in.read(dstbyteArray);
            bytesWritten = 0;
            int i = prl_length;
            int j;
            byte abyte0[];
            if (prl_length > 200) {
                i = 200;
                j = 1;
                abyte0 = new byte[i];
            } else {
                j = 0;
                abyte0 = new byte[prl_length];
            }

            log((new StringBuilder()).append("prl_length:").append(prl_length)
                    .toString());

            prlbytedata.get(abyte0, 0, abyte0.length);
            ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
            DataOutputStream dataoutputstream = new DataOutputStream(
                    bytearrayoutputstream);
            fileSize = 8 + abyte0.length;

            log((new StringBuilder()).append("bytesWritten : ")
                    .append(bytesWritten).append(" / dstArray.length : ")
                    .append(abyte0.length).append(" / more : ").append(0)
                    .append(" /filesize : ").append(fileSize).toString());

            dataoutputstream.writeByte(OEM_FUNCTION_ID_OMADM);
            dataoutputstream.writeByte(OEM_OMADM_WRITE_PRL);
            dataoutputstream.writeShort(fileSize);
            dataoutputstream.writeShort(prl_length);
            dataoutputstream.writeByte(abyte0.length);
            dataoutputstream.writeByte(j);
            dataoutputstream.write(abyte0);
            invokeOemRilRequestRaw(bytearrayoutputstream.toByteArray(),
                    mHandler.obtainMessage(OMADM_PRL_RESP));
            bytesWritten = bytesWritten + abyte0.length;
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
            sendEmptyMessageToUI(NOTIFY_WRITE_PRL_FAILED);
        }
        return;
    }

    private static void log(String s) {
        Log.i(TAG, s);
    }

    private void initPhoneHelper() {
        log("Loading internal telephony methods via reflection ;D ");
        try {
            // internal telephony
            Class<?> PhoneFactory = Class
                    .forName("com.android.internal.telephony.PhoneFactory");
            Class<?> Phone = Class
                    .forName("com.android.internal.telephony.Phone");
            Method getDefaultPhone = PhoneFactory.getDeclaredMethod(
                    "getDefaultPhone", (Class[]) null);
            mPhone = Phone.cast(getDefaultPhone.invoke(PhoneFactory,
                    (Object[]) null));
            invokeOemRilRequestRaw = Phone.getDeclaredMethod(
                    "invokeOemRilRequestRaw", byte[].class, Message.class);
            setRadioPower = Phone.getDeclaredMethod("setRadioPower",
                    boolean.class);
            getCdmaPrlVersion = Phone.getDeclaredMethod("getCdmaPrlVersion",
                    (Class[]) null);
            // asyncresult
            AsyncResult = Class.forName("android.os.AsyncResult");
            userObj = AsyncResult.getDeclaredField("userObj");
            exception = AsyncResult.getDeclaredField("exception");
            result = AsyncResult.getDeclaredField("result");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getCdmaPrlVersion() {
        String prl = "";
        try {
            prl = (String) getCdmaPrlVersion.invoke(mPhone, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return prl;
    }

    private void setRadioPower(boolean on) {
        try {
            setRadioPower.invoke(mPhone, on);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void invokeOemRilRequestRaw(byte[] stream, Message message) {
        try {
            invokeOemRilRequestRaw.invoke(mPhone, stream, message);
            log("Message sent to ril");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
