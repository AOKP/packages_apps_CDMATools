
package org.teameos.settings.device;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;

public abstract class RadioPreference extends Preference {
    private static final String TAG = "RadioPreference";
    protected ServiceConnection mConnection;
    protected ServiceHandler mHandler = new ServiceHandler();
    protected Messenger mServiceMessenger;
    protected Messenger mClientMessenger = new Messenger(mHandler);
    protected Context mContext;
    protected Intent service;

    private boolean isBound = false;

    public RadioPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
        mContext = context;
        service = new Intent().setClass(mContext, PhoneService.class);
        mConnection = new ServiceConnection() {

            public void onServiceDisconnected(ComponentName name) {
                log("Service disconnected");
                isBound = false;
            }

            public void onServiceConnected(ComponentName name, IBinder service) {
                log("Service connected");
                isBound = true;
                mServiceMessenger = new Messenger(service);
                Message register = Message.obtain(null,
                        PhoneService.MSG_REGISTER_CLIENT);
                register.replyTo = mClientMessenger;
                try {
                    mServiceMessenger.send(register);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                handleBind();
            }
        };
    }

    protected void handleServiceMessage(Message message) {
    }

    protected void handleBind() {
    }

    protected void sendMessageToService(int what, String key, String value) {
        try {
            Bundle b = new Bundle();
            b.putString(key, value);
            Message msg = Message.obtain(null, what);
            msg.setData(b);
            msg.replyTo = mClientMessenger;
            mServiceMessenger.send(msg);
        } catch (RemoteException e) {
        }
    }

    protected void sendEmptyMessageToService(int what) {
        try {
            Message msg = Message.obtain(null, what);
            msg.replyTo = mClientMessenger;
            mServiceMessenger.send(msg);
        } catch (RemoteException e) {
        }
    }

    protected void sendIntMessageToService(int what, String key, int value) {
        try {
            Bundle b = new Bundle();
            b.putInt(key, value);
            Message msg = Message.obtain(null, what);
            msg.setData(b);
            msg.replyTo = mClientMessenger;
            mServiceMessenger.send(msg);
        } catch (RemoteException e) {
        }
    }

    protected void bind() {
        log("binding to service");
        if (isBound) {
            handleBind();
        } else {
            mContext.bindService(service, mConnection, Context.BIND_AUTO_CREATE);
        }

    }

    private class ServiceHandler extends Handler {
        public void handleMessage(Message msg) {
            handleServiceMessage(msg);
        }
    }

    private static void log(String s) {
        Log.i(TAG, s);
    }
}
