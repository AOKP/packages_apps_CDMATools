
package org.teameos.settings.device;

import android.content.Context;

import java.io.File;

public class InitHelper {
    public static final String CRESPO4G = "crespo4g";
    public static final String TOROPLUS = "toroplus";
    public static final String TORO = "toro";
    public static final String NOT_FOUND = "notfound";

    private static final int TUNA_OEM_MAIN_CMD_HIDDEN = 81;
    private static final int CRESPO_OEM_MAIN_CMD_HIDDEN = 9;

    public static String getDevice(Context context) {
        return SystemPropertiesProxy.get(context, "ro.product.device", NOT_FOUND);
    }

    public static boolean isTuna(Context context) {
        return isToro(context) || isToroplus(context);
    }

    public static boolean isToro(Context context) {
        return TORO.equals(getDevice(context));
    }

    public static boolean isToroplus(Context context) {
        return TOROPLUS.equals(getDevice(context));
    }

    public static boolean isCrespo(Context context) {
        return CRESPO4G.equals(getDevice(context));
    }

    public static boolean isGrouper(Context context) {
        return true;
    }

    public static boolean hasHiddenMenu() {
        return new File("/system/app/HiddenMenu.apk").exists();
    }

    public static boolean hasSprintMenu() {
        return new File("/system/app/SprintMenu.apk").exists();
    }

    public static boolean hasSDM() {
        return new File("/system/app/SDM.apk").exists();
    }

    public static boolean hasSyncService() {
        return new File("/system/app/SyncMLSvc.apk").exists();
    }

    public static int getOemMainCommandHidden(Context context) {
        if (isTuna(context)) {
            return TUNA_OEM_MAIN_CMD_HIDDEN;
        } else if (isCrespo(context)) {
            return CRESPO_OEM_MAIN_CMD_HIDDEN;
        } else
            return -1;
    }

    public static String getCrespoPrl(Context context) {
        return getPRL(context);
    }

    private static String getPRL(Context context) {
        String s = SystemPropertiesProxy.get(context, "ril.prl_ver_1");
        String s1;
        if (s != null && s.length() > 2)
            s1 = s.substring(2, s.length());
        else
            s1 = "PRL : <unknown>";
        return s1;
    }
}
