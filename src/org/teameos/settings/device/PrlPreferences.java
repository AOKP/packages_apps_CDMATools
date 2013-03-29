
package org.teameos.settings.device;

import android.os.Bundle;

public class PrlPreferences extends PreferenceListFragment {

    public static PrlPreferences newInstance(int xml) {
        PrlPreferences f = new PrlPreferences(xml);
        Bundle b = new Bundle();
        b.putInt("xml", xml);
        f.setArguments(b);
        return f;
    }

    public PrlPreferences(int xmlId) {
        // TODO Auto-generated constructor stub
        super(xmlId);
    }
    
    public PrlPreferences() {
        super();
    }
}
