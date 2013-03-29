
package org.teameos.settings.device;

import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;

import org.teameos.settings.device.InitHelper;

public class PreferenceFilter {
    public static void filterMenus(PreferenceScreen root) {
    }

    public static void filterDiagnostics(PreferenceScreen root) {
        PreferenceCategory diagCategory = (PreferenceCategory) root.findPreference("eos_diag");
        PreferenceCategory advancedCategory = (PreferenceCategory) root
                .findPreference("eos_advanced_menus");
        if (diagCategory != null && (!InitHelper.isToroplus(root.getContext())
                || !InitHelper.isCrespo(root.getContext()))) {
            Preference mslPref = diagCategory.findPreference("msl_acquire");
            if (mslPref != null) {
                diagCategory.removePreference(mslPref);
            }
        }
        if(diagCategory != null && !InitHelper.isTuna(root.getContext())) {
            Preference diagMode = diagCategory.findPreference("eos_sprint_diag");
            if (diagMode != null) {
                diagCategory.removePreference(diagMode);
            }
        }
        if (advancedCategory != null && !InitHelper.isToroplus(root.getContext())) {
            root.removePreference(advancedCategory);
        }

    }

    public static void filterRoam(PreferenceScreen root) {
        PreferenceCategory prlCategory = (PreferenceCategory) root.findPreference("prl_management");
        if (prlCategory != null) {
            Preference oldSchool = prlCategory.findPreference("eos_sprint_write_prl");
            if (oldSchool != null && !InitHelper.isToroplus(root.getContext())) {
                prlCategory.removePreference(oldSchool);
            }

        }
    }
}
