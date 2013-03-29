
package org.teameos.settings.device;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.os.Bundle;
import android.os.Message;

public class MslPreference extends RadioPreference implements
        Preference.OnPreferenceClickListener {
    private static String TAG = "MslPreference";
    private String MSL = "";
    private String msl_error = "Error retrieving MSL";
    private String msl_prefix = "";

    public MslPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        msl_prefix = mContext.getResources()
                .getString(R.string.msl_code_prefix);
        setOnPreferenceClickListener(this);
    }

    public MslPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        // TODO Auto-generated constructor stub
    }

    public MslPreference(Context context) {
        this(context, null);
        // TODO Auto-generated constructor stub
    }
    
    protected void handleBind() {
        sendEmptyMessageToService(PhoneService.REQUEST_GET_MSL);
    }

    private static void log(String s) {
        Log.i(TAG, s);
    }

    protected void handleServiceMessage(Message msg) {
        switch (msg.what) {
            case PhoneService.NOTIFY_GET_MSL:
                log("incoming msl message!");
                Bundle b = msg.getData();
                MSL = msl_error;
                if (b != null) {
                    String msl = b.getString(PhoneService.NOTIFY_MSL_VALUE_KEY);
                    MSL = new StringBuilder().append(msl_prefix).append(" ").append(msl)
                            .toString();
                    log("msl acquired successfully!");
                }
                setSummary(MSL);
                break;
            case PhoneService.NOTIFY_GET_MSL_FAILED:
                MSL = msl_error;
                log("msl message failed");
                setSummary(MSL);
                break;
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        log("binding to service");
        bind();
        return true;
    }
}
