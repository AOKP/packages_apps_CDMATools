
package org.teameos.settings.device;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;

import org.teameos.settings.device.PreferenceListFragment.OnPreferenceAttachedListener;
import org.teameos.settings.device.PrlDirectoryDialogFragment.OnPrlDirectoryChangedListener;
import org.teameos.settings.device.PrlListDialogFragment.OnPrlSelectedListener;

public class DeviceSettings extends FragmentActivity
        implements OnPreferenceAttachedListener {

    static String TAG = "CDMATools";
    FragmentManager fm;
    FragmentTransaction ft;
    PrlWriterPreference prlWriterPref;
    PrlSetDirectoryPreference prlDirectoryPref;
    OnPrlDirectoryChangedListener directoryListener;
    OnPrlSelectedListener prlListListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_settings);
        fm = getSupportFragmentManager();
        ViewPagerAdapter adapter = new ViewPagerAdapter(getApplicationContext(),
                getSupportFragmentManager());
        ViewPager pager = (ViewPager) findViewById(R.id.viewpager);
        TitlePageIndicator indicator = (TitlePageIndicator) findViewById(R.id.indicator);
        pager.setAdapter(adapter);
        indicator.setViewPager(pager);
        directoryListener = new OnPrlDirectoryChangedListener() {
            @Override
            public void onPrlDirectoryChanged(String path) {
                handleDirectoryChanged(path);                
            }            
        };
        prlListListener = new OnPrlSelectedListener() {
            @Override
            public void onPrlSelected(String prl) {
                handlePrlSelected(prl);                
            }
        };
        startService(new Intent().setClass(this, PhoneService.class));
    }

    @Override
    public void onPause() {
        super.onPause();
        stopService(new Intent().setClass(this, PhoneService.class));
    }

    @Override
    public void onResume() {
        super.onResume();
        PrlListDialogFragment.setOnPrlSelectedListener(prlListListener);
        PrlDirectoryDialogFragment.setOnPrlDirectoryChangedListener(directoryListener);
        startService(new Intent().setClass(this, PhoneService.class));
    }

    @Override
    public void onPreferenceAttached(PreferenceScreen root, int xmlId) {
        if (root == null) {
            Log.i(TAG, "Root preference screen is null!");
        } else if (xmlId == R.xml.updates_and_menus) {
            Log.i(TAG, "Updates and Menus attached");
            PreferenceFilter.filterMenus(root);
            return;
        } else if (xmlId == R.xml.diagnostics) {
            Log.i(TAG, "Diagnostics is attached");
//            PreferenceFilter.filterDiagnostics(root);
            return;
        } else if (xmlId == R.xml.prl_management) {
            Log.i(TAG, "PRL Management is attached");
            PreferenceFilter.filterRoam(root);
            prlWriterPref = (PrlWriterPreference) root.findPreference("write_prl");
            if (prlWriterPref != null) {
                prlWriterPref
                        .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                            @Override
                            public boolean onPreferenceClick(Preference preference) {
                                showPrlListDialog();
                                return false;
                            }

                        });
                prlWriterPref.requestUpdatePrl();
            }
            prlDirectoryPref = (PrlSetDirectoryPreference) root.findPreference("set_prl_dir");
            if (prlDirectoryPref != null) {
                prlDirectoryPref
                        .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                            @Override
                            public boolean onPreferenceClick(Preference preference) {
                                showPrlDirectoryDialog();
                                return false;
                            }
                        });
            }
            return;
        }
    }
    
    public void handlePrlSelected(String prl) {
        if (prlWriterPref != null)
            prlWriterPref.sendPrlToService(prl);  
    }
    
    public void handleDirectoryChanged(String path) {
        if (prlDirectoryPref != null)
            prlDirectoryPref.updateSummary(path);  
    }

    public void showPrlListDialog() {
        PrlListDialogFragment prlListDialog = PrlListDialogFragment.newInstance();
        PrlListDialogFragment.setOnPrlSelectedListener(prlListListener);
        prlListDialog.show(fm.beginTransaction(), "prl_list");
    }

    public void showPrlDirectoryDialog() {
        PrlDirectoryDialogFragment prlDirectoryDialog = PrlDirectoryDialogFragment.newInstance();
        PrlDirectoryDialogFragment.setOnPrlDirectoryChangedListener(directoryListener);
        prlDirectoryDialog.show(fm.beginTransaction(), "prl_dir");
    }
}
