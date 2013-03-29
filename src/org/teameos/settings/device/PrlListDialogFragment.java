
package org.teameos.settings.device;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;

public class PrlListDialogFragment extends DialogFragment {
    private static String TAG = "PrlListDialogFragment";
    PrlAdapter mPrlAdapter;
    SharedPreferences mPrefs;

    public static PrlListDialogFragment newInstance() {
        PrlListDialogFragment f = new PrlListDialogFragment();
        return f;
    }

    public PrlListDialogFragment() {
    }

    public interface OnPrlSelectedListener {
        public void onPrlSelected(String prl);
    }
    
    static OnPrlSelectedListener mListener;

    public static void setOnPrlSelectedListener(OnPrlSelectedListener listener) {
        mListener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPrefs = getActivity().getSharedPreferences(PrlDirectoryDialogFragment.TAG,
                Context.MODE_PRIVATE);
        log("Created");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPrlAdapter == null) {
            mPrlAdapter = new PrlAdapter(getActivity(), getPrlFiles());
        }
        mPrlAdapter.clear();
        mPrlAdapter.addAll(getPrlFiles());
        mPrlAdapter.notifyDataSetChanged();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        log("onCreateDialog called");
        mPrlAdapter = new PrlAdapter(getActivity(), getPrlFiles());

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.select_prl);
        builder.setAdapter(mPrlAdapter, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                File prl = (File) mPrlAdapter.getItem(item);
                mListener.onPrlSelected(prl.getAbsolutePath());
            }
        });
        return builder.create();
    }

    private class PrlAdapter extends ArrayAdapter<File> {
        private final ArrayList<File> prls;
        private final Context mContext;

        public PrlAdapter(Context context, ArrayList<File> prls) {
            super(context, android.R.layout.select_dialog_item, prls);
            this.mContext = context;
            this.prls = prls;
            // TODO Auto-generated constructor stub
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View itemRow = convertView;
            File f = prls.get(position);
            itemRow = ((LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(android.R.layout.select_dialog_item, null);
            ((TextView) itemRow.findViewById(android.R.id.text1)).setText(f.getName());

            return itemRow;
        }
    }

    private ArrayList<File> getPrlFiles() {
        String path = mPrefs.getString(PrlDirectoryDialogFragment.FOLDER_KEY,
                PrlDirectoryDialogFragment.SDCARD_PATH);
        if (!path.endsWith("/")) {
            path = new StringBuilder().append(path).append("/").toString();
        }
        File f = new File(path);
        if (!f.exists()) {
            log(f.toString()
                    + " does not exist? Either user deleted or we have problems if sdcard does not exist. returning null");
            return null;
        }
        if (!f.isDirectory()) {
            log(f.toString() + " is not a directory? Setting to root of sdcard");
            String rootPath = PrlDirectoryDialogFragment.SDCARD_PATH;
            if (!rootPath.endsWith("/")) {
                rootPath = new StringBuilder().append(rootPath).append("/").toString();
            }
            f = new File(rootPath);
        }
        ArrayList<File> list = new ArrayList<File>();
        if (f.listFiles() == null)
            return null;
        for (File tmp : f.listFiles(new PrlFilter())) {
            list.add(tmp);
        }
        return list;
    }

    private class PrlFilter implements FileFilter {
        private final String[] okFileExtensions =
                new String[] {
                    "prl"
                };

        @Override
        public boolean accept(File file) {
            for (String extension : okFileExtensions) {
                if (file.getName().toLowerCase().endsWith(extension)) {
                    return true;
                }
            }
            return false;
        }
    }

    private static void log(String s) {
        Log.i(TAG, s);
    }
}
