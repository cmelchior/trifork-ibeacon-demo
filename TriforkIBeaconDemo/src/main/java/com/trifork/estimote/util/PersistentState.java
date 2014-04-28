package com.trifork.estimote.util;

import android.content.Context;
import android.content.SharedPreferences;
import com.estimote.sdk.Beacon;
import com.estimote.sdk.Region;

public class PersistentState  {

    private static final String PREFERENCE_FILE_NAME = "ibeacon_demo_settings";
    private static final String KEY_UUID = "uuid";
    private static final String KEY_MAJOR = "major";
    private static final String KEY_MINOR = "minor";

    private SharedPreferences prefs;

    public PersistentState(Context context) {
        prefs = context.getSharedPreferences(PREFERENCE_FILE_NAME, Context.MODE_PRIVATE);
    }

    public Region getSelectedBeacon() {
        String uuid = prefs.getString(KEY_UUID, "");
        int major = prefs.getInt(KEY_MAJOR, 0);
        int minor = prefs.getInt(KEY_MINOR, 0);
        if (uuid.equals("")) {
            return null;
        } else {
            return new Region(generateLabel(uuid,major,minor), uuid, major, minor);
        }
    }

    public void setSelectedBeacon(Beacon beacon) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_UUID,beacon.getProximityUUID());
        editor.putInt(KEY_MAJOR, beacon.getMajor());
        editor.putInt(KEY_MINOR, beacon.getMinor());
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