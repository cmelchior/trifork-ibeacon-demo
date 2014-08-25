package com.trifork.ibeacon.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Looper;

import com.trifork.ibeacon.BuildConfig;

import org.altbeacon.beacon.Region;

import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Dao {

    private static final String TABLE_URI = "content://" + BuildConfig.PACKAGE_NAME + "/RegionHistory";

    private final Database database;
    private final Context context;
    private SQLiteDatabase db;
    private ExecutorService threadPool;

    public Dao(Database database, Context context) {
        this.database = database;
        this.context = context;
        threadPool = Executors.newCachedThreadPool();
    }

    public void open() {
        if (db == null || !db.isOpen()) {
            db = database.getWritableDatabase();
        }
    }

    public void close() {
        if (db != null) {
            db.close();
        }
    }

    /**
     * Helper methods for running methods on the dao outside the GUI thread.
     * The Dao maintains a ThreadPool that clients can use.
     */
    public void execute(Runnable command) {
        threadPool.execute(command);
    }

    public synchronized RegionHistoryEntry enterRegion(Region region) {
        assertAcccess();

        ContentValues values = new ContentValues();
        values.put(Database.COLUMN_UUID, region.getId1().toString());
        values.put(Database.COLUMN_MAJOR, region.getId2().toInt());
        values.put(Database.COLUMN_MINOR, region.getId3().toInt());
        values.put(Database.COLUMN_REGIONNAME, region.getUniqueId());
        values.put(Database.COLUMN_ENTER, Calendar.getInstance().getTime().getTime());
        values.put(Database.COLUMN_EXIT, (Long) null);

        long id = db.insert(Database.TABLE_REGIONS, null, values);
        if (id > -1) {
            notifyChange(TABLE_URI);
        }
        return getEntry(id);
    }

    public synchronized boolean exitRegion(RegionHistoryEntry region) {
        assertAcccess();

        region.setExit(Calendar.getInstance().getTime().getTime());

        ContentValues values = new ContentValues();
        values.put(Database.COLUMN_UUID, region.getUuid());
        values.put(Database.COLUMN_MAJOR, region.getMajor());
        values.put(Database.COLUMN_MINOR, region.getMinor());
        values.put(Database.COLUMN_REGIONNAME, region.getName());
        values.put(Database.COLUMN_ENTER, region.getEnter());
        values.put(Database.COLUMN_EXIT, region.getExit());

        int result = db.update(Database.TABLE_REGIONS, values, Database.COLUMN_ID + "=?", new String[]{Long.toString(region.getId())});
        boolean success = result == 1;
        if (success) {
            notifyChange(getItemUri(region));
        }
        return success;
    }

    /**
     * Returns the HistoryEntry for the given ID or null if it couldn't be found or an error occured.
     */
    private RegionHistoryEntry getEntry(long id) {
        assertAcccess();
        if (id < 0) return null;

        Cursor c = db.query(false,
                Database.TABLE_REGIONS,
                Database.ALL_COLUMNS,
                Database.COLUMN_ID + "=?",
                new String[] { Long.toString(id) },
                null,
                null,
                null,
                null);

        if (c.getCount() == 1) {
            c.moveToFirst();
            RegionHistoryEntry entry = getEntryFromCursor(c);
            c.close();
            return entry;

        } else {
            c.close();
            return null;
        }
    }

    /**
     * Converts between a cursor and the history entry model.
     * The current index of the cursor will be used.
     */
    public RegionHistoryEntry getEntryFromCursor(Cursor c) {
        long id = c.getLong(c.getColumnIndexOrThrow(Database.COLUMN_ID));
        String uuid = c.getString(c.getColumnIndexOrThrow(Database.COLUMN_UUID));
        int major = c.getInt(c.getColumnIndexOrThrow(Database.COLUMN_MAJOR));
        int minor = c.getInt(c.getColumnIndexOrThrow(Database.COLUMN_MINOR));
        String name = c.getString(c.getColumnIndexOrThrow(Database.COLUMN_REGIONNAME));
        long enter = c.getLong(c.getColumnIndexOrThrow(Database.COLUMN_ENTER));
        long exit = c.getLong(c.getColumnIndexOrThrow(Database.COLUMN_EXIT));
        return new RegionHistoryEntry(id, uuid, major, minor, name, enter, exit);
    }

    public Cursor getHistory(Region region) {
        assertAcccess();
        if (region == null) return null;
        Cursor c = db.query(false,
                Database.TABLE_REGIONS,
                Database.ALL_COLUMNS,
                Database.COLUMN_UUID +  "=? AND " + Database.COLUMN_MAJOR + "=? AND " + Database.COLUMN_MINOR + "=?",
                new String[] { region.getId1().toString(), region.getId2().toString(), region.getId3().toString() },
                null,
                null,
                null,
                null);

        c.setNotificationUri(context.getContentResolver(), Uri.parse(TABLE_URI));
        return c;
    }

    /**
     * Clears the log for the selected beacon. If null all logs are cleared.
     */
    public void clearLog(Region beacon) {
        assertAcccess();

        int result  = db.delete(
                Database.TABLE_REGIONS,
                Database.COLUMN_UUID +  "=? AND " + Database.COLUMN_MAJOR + "=? AND " + Database.COLUMN_MINOR + "=?",
                new String[] { beacon.getId1().toString(), beacon.getId2().toString(), beacon.getId3().toString() }
        );

        if (result > 0) {
            notifyChange(TABLE_URI);
        }
    }


    private String getItemUri(RegionHistoryEntry region) {
        return TABLE_URI.toString() + "/" + region.getId();
    }

    private void notifyChange(String uri) {
        Uri changeUri = Uri.parse(uri);
        context.getContentResolver().notifyChange(changeUri, null);
    }

    private void assertAcccess() {
        if (Looper.getMainLooper().getThread() == Thread.currentThread()) throw new RuntimeException("Access in GUI thread not allowed");
        if (!db.isOpen()) throw new RuntimeException("Database isn't open");
    }

}
