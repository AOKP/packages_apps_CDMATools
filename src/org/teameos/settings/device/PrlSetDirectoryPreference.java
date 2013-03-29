
package org.teameos.settings.device;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.util.AttributeSet;

public class PrlSetDirectoryPreference extends Preference {
    SharedPreferences mPrefs;
    Context mContext;

    public PrlSetDirectoryPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
        mContext = context;
        mPrefs = mContext
                .getSharedPreferences(PrlDirectoryDialogFragment.TAG, Context.MODE_PRIVATE);
        String summary = mPrefs.getString(PrlDirectoryDialogFragment.FOLDER_KEY,
                PrlDirectoryDialogFragment.SDCARD_PATH);
        updateSummary(summary);
    }

    public PrlSetDirectoryPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        // TODO Auto-generated constructor stub
    }

    public PrlSetDirectoryPreference(Context context) {
        this(context, null);
        // TODO Auto-generated constructor stub
    }

    public void updateSummary(String value) {
        setSummary(new StringBuilder()
                .append(mContext.getResources().getString(R.string.current_folder_name))
                .append(" ")
                .append(value)
                .toString());
    }
}
