
package org.teameos.settings.device;

import android.os.Bundle;

public class DiagPreferences extends PreferenceListFragment {

    public static DiagPreferences newInstance(int xml) {
        DiagPreferences f = new DiagPreferences(xml);
        Bundle b = new Bundle();
        b.putInt("xml", xml);
        f.setArguments(b);
        return f;
    }

    public DiagPreferences(int xmlId) {
        // TODO Auto-generated constructor stub
        super(xmlId);
    }

    public DiagPreferences() {
        super();
    }
}
