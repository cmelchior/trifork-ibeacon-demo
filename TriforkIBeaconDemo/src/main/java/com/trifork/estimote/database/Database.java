package com.trifork.estimote.database;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Database extends SQLiteOpenHelper {

    private final static String TAG = Database.class.getName();

    public static final String TABLE_REGIONS = "regions";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_REGIONNAME = "name";
    public static final String COLUMN_UUID = "uuid";
    public static final String COLUMN_MAJOR = "major";
    public static final String COLUMN_MINOR = "minor";
    public static final String COLUMN_ENTER = "enter";
    public static final String COLUMN_EXIT = "exit";

    private static final String DATABASE_NAME = "regions.db";
    private static final int DATABASE_VERSION = 1;
    public static final String[] ALL_COLUMNS = new String[]{COLUMN_ID, COLUMN_UUID, COLUMN_MAJOR, COLUMN_MINOR, COLUMN_REGIONNAME, COLUMN_ENTER, COLUMN_EXIT};

    private AssetManager am;

    public Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        am = context.getAssets();
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        importSQL("regions.db.sql", database);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REGIONS);
        onCreate(db);
    }

    private void importSQL(String fileName, SQLiteDatabase db) {
        InputStream is = null;
        BufferedReader reader = null;
        try {

            is = am.open(fileName);
            reader = new BufferedReader(new InputStreamReader(is, "utf8"));
            StringBuilder sb = new StringBuilder();
            String s;

            while ((s = reader.readLine()) != null) {
                sb.append(s);
                if (s.endsWith(";")) {
                    db.execSQL(sb.toString());
                    sb.setLength(0);
                }
            }

            // Create database
            Log.d(TAG, fileName + " imported.");

        } catch (IOException e) {
            Log.e(TAG, "Failed to access DB schema file", e);
        } catch (SQLException e) {
            Log.e(TAG, "Could not import file (" + fileName + "): ", e);
        } finally {
            try {
                reader.close();
                is.close();
            } catch (Exception e) {
                // Ignore
            }
        }
    }
}