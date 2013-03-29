
package org.teameos.settings.device;

import android.os.Bundle;

public class UpdatesPreferences extends PreferenceListFragment {

    public static UpdatesPreferences newInstance(int xml) {
        UpdatesPreferences f = new UpdatesPreferences(xml);
        Bundle b = new Bundle();
        b.putInt("xml", xml);
        f.setArguments(b);
        return f;
    }

    public UpdatesPreferences(int xmlId) {
        super(xmlId);
    }

    public UpdatesPreferences() {
        super();
    }
}
