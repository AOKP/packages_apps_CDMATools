
package org.teameos.settings.device;

import android.content.Context;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.util.AttributeSet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class DiagnosticModePreference extends CheckBoxPreference implements
        Preference.OnPreferenceChangeListener {

    private static final String USB_SWITCH_PATH = "/sys/class/android_usb/android0/enable";
    private static final String USB_MODE_PATH = "/sys/devices/tuna_otg/usb_sel";
    private static final String PDA_MODE = "PDA";
    private static final String MODEM_MODE = "MODEM";
    private static final String SWITCH_OFF = "0";
    private static final String SWITCH_ON = "1";

    public DiagnosticModePreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
        setChecked(isDiagEnabled());
        setOnPreferenceChangeListener(this);
        // TODO Auto-generated constructor stub
    }

    public DiagnosticModePreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        // TODO Auto-generated constructor stub
    }

    public DiagnosticModePreference(Context context) {
        this(context, null);
        // TODO Auto-generated constructor stub
    }

    private void toggleDiag(boolean enabled) {
        String mode = null;
        mode = enabled ? MODEM_MODE : PDA_MODE;
        if (mode != null) {
            setUsbSwitch(SWITCH_OFF);
            setDiagMode(mode);
            setUsbSwitch(SWITCH_ON);
        }
    }

    private void setUsbSwitch(String mode) {
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter(USB_SWITCH_PATH));
            String output = "" + mode;
            writer.write(output.toCharArray(), 0, output.toCharArray().length);
            writer.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void setDiagMode(String mode) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(USB_MODE_PATH));
            String output = "" + mode;
            writer.write(output.toCharArray(), 0, output.toCharArray().length);
            writer.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private boolean isDiagEnabled() {
        String currentVal;
        try {
            File f = new File(USB_MODE_PATH);
            BufferedReader reader = new BufferedReader(new FileReader(f));
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            int i;
            i = reader.read();
            while (i != -1)
            {
                byteArrayOutputStream.write(i);
                i = reader.read();
            }
            reader.close();
            currentVal = byteArrayOutputStream.toString();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            currentVal = PDA_MODE;
            e.printStackTrace();
        }
        if (currentVal.equals(PDA_MODE)) {
            return false;
        } else if (currentVal.equals(MODEM_MODE)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        // TODO Auto-generated method stub
        toggleDiag((Boolean) newValue);
        return true;
    }
}
