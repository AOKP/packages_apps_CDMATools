
package org.teameos.settings.device;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Toast;

public class PrlWriterPreference extends RadioPreference {
    private static String TAG = "PrlWriterPreference";
    private String prl;
    private boolean isWritingPrl = false;
    private boolean isRequestPrl = false;

    public PrlWriterPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        Log.i(TAG, "Constructor called");
    }

    public PrlWriterPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        // TODO Auto-generated constructor stub
    }

    public PrlWriterPreference(Context context) {
        this(context, null);
        // TODO Auto-generated constructor stub
    }

    public void sendPrlToService(String filepath) {
        log("Received prl from main activity dialog callback");
        prl = filepath;
        isWritingPrl = true;
        bind();
    }

    public void requestUpdatePrl() {
        log("Received request prl update");
        isRequestPrl = true;
        bind();
    }

    private void updateSummary(String value) {
        setSummary(new StringBuilder().append("Current PRL: ")
                .append(value).toString());
    }

    protected void handleBind() {
        if (isWritingPrl) {
            sendMessageToService(PhoneService.REQUEST_START_PRL,
                    PhoneService.REQUEST_PRL_PATH_KEY,
                    prl);
        } else if (isRequestPrl) {
            sendEmptyMessageToService(PhoneService.REQUEST_UPDATE_PRL);
        }
    }

    private static void log(String s) {
        Log.i(TAG, s);
    }

   protected void handleServiceMessage(Message msg) {
        Bundle b = msg.getData();
        switch (msg.what) {
            case PhoneService.NOTIFY_WRITE_PRL_START:
                log("Message from PhoneService: Starting PRL Flash");
                break;
            case PhoneService.NOTIFY_WRITE_PRL_UPDATE:
                int bytes = 0;
                if(b != null) {
                    bytes = b.getInt(PhoneService.NOTIFY_WRITE_PRL_UPDATE_KEY, 0);
                }
                setSummary("Bytes written: " + String.valueOf(bytes));
                log("Bytes written " + String.valueOf(bytes));
                break;
            case PhoneService.NOTIFY_WRITE_PRL_DONE:
                log("PRL flash done");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        setSummary("PRL will update shortly");
                    }
                }, 2 * 1000);
                Toast.makeText(mContext, "Flash successful, restarting radio", Toast.LENGTH_SHORT).show();
                isWritingPrl = false;
                break;
            case PhoneService.NOTIFY_WRITE_PRL_FAILED:
                log("Prl flash failed! Probably a bad file path");
                isWritingPrl = false;
                break;
            case PhoneService.NOTIFY_UPDATE_PRL_NUMBER:
                if (b != null) {
                    String prl = b.getString(PhoneService.NOTIFY_PRL_VALUE_KEY, "Unknown");
                    updateSummary(prl);
                }
                isRequestPrl = false;
                break;
        }
    }
}
