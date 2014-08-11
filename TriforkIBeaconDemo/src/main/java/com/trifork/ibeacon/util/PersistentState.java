package com.trifork.ibeacon.util;

import android.content.Context;
import android.content.SharedPreferences;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.Region;

public class PersistentState  {

    private static final String PREFERENCE_FILE_NAME = "ibeacon_demo_settings";
    private static final String KEY_UUID = "uuid";
    private static final String KEY_MAJOR = "major";
    private static final String KEY_MINOR = "minor";

    private SharedPreferences prefs;

    public PersistentState(Context context) {
        prefs = context.getSharedPreferences(PREFERENCE_FILE_NAME, Context.MODE_PRIVATE);
    }

    public Region getSelectedRegion() {
        String uuid = prefs.getString(KEY_UUID, "");
        int major = prefs.getInt(KEY_MAJOR, 0);
        int minor = prefs.getInt(KEY_MINOR, 0);
        if (uuid.equals("")) {
            return null;
        } else {
            return new Region(generateLabel(uuid,major,minor),
                    Identifier.parse(uuid),
                    Identifier.fromInt(major),
                    Identifier.fromInt(minor));
        }
    }

    public void setSelectedRegion(Beacon beacon) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_UUID,beacon.getId1().toString());
        editor.putInt(KEY_MAJOR, beacon.getId2().toInt());
        editor.putInt(KEY_MINOR, beacon.getId3().toInt());
        editor.commit();
    }

    private String generateLabel(String uuid, int major, int minor) {
        if (uuid.length() < 10) {
            return uuid;
        } else {
            return uuid.substring(0, 5) + ":" + uuid.substring(uuid.length() - 5, uuid.length());
         }
    }
}