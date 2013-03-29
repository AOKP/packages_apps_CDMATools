
package org.teameos.settings.device;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;

public class PrlDirectoryDialogFragment extends DialogFragment {
    public static String TAG = "PrlDirectory";
    public static String FOLDER_KEY = "folder_key";
    public static String SDCARD_PATH = "/data/media";
    SharedPreferences mPrefs;
    SharedPreferences.Editor mEdit;

    public static PrlDirectoryDialogFragment newInstance() {
        PrlDirectoryDialogFragment f = new PrlDirectoryDialogFragment();
        return f;
    }

    public PrlDirectoryDialogFragment() {
    }

    public interface OnPrlDirectoryChangedListener {
        public void onPrlDirectoryChanged(String path);
    }
    
    static OnPrlDirectoryChangedListener mListener;

    public static void setOnPrlDirectoryChangedListener(OnPrlDirectoryChangedListener listener) {
        mListener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPrefs = getActivity().getSharedPreferences(TAG, Context.MODE_PRIVATE);
        mEdit = mPrefs.edit();
        log("Created");
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        log("onCreateDialog called");
        String prlPath = getPrlDir();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Set path");
        final EditText input = new EditText(getActivity());
        input.setText(prlPath);
        builder.setView(input);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString();
                if (!value.endsWith("/")) {
                    StringBuilder b = new StringBuilder();
                    b.append(value)
                            .append("/");
                    value = b.toString();
                }
                File test = new File(value);
                if (test.exists() && test.isDirectory()) {
                    mEdit.putString(FOLDER_KEY, value);
                    mEdit.apply();
                    mListener.onPrlDirectoryChanged(value);
                } else {
                    Toast.makeText(getActivity(), "Select a valid directory", Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });
        return builder.create();
    }

    private String getPrlDir() {
        String path = mPrefs.getString(FOLDER_KEY, SDCARD_PATH);
        if (!path.endsWith("/")) {
            path = new StringBuilder().append(path).append("/").toString();
        }
        File f = new File(path);
        if (!f.exists()) {
            log(f.toString()
                    + " does not exist? Either user deleted or we have problems if sdcard does not exist. returning null");
            return "The universe just failed!";
        }
        if (!f.isDirectory()) {
            log(f.toString() + " is not a directory? Setting to root of sdcard");
            String rootPath = SDCARD_PATH;
            if (!rootPath.endsWith("/")) {
                rootPath = new StringBuilder().append(rootPath).append("/").toString();
                return rootPath;
            }
        }
        return path;
    }

    private static void log(String s) {
        Log.i(TAG, s);
    }

}
