package com.trifork.estimote.database;

/**
 * Model of a visited region in the database
 */
public class RegionHistoryEntry {

    public long id;

    public String uuid;
    public int major;
    public int minor;

    public String name;
    public long enter;
    public long exit;

    public RegionHistoryEntry(long id, String uuid, int major, int minor, String name, long enter, long exit) {
        this.id = id;
        this.uuid = uuid;
        this.major = major;
        this.minor = minor;
        this.name = name;
        this.enter = enter;
        this.exit = exit;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getEnter() {
        return enter;
    }

    public void setEnter(long enter) {
        this.enter = enter;
    }

    public long getExit() {
        return exit;
    }

    public void setExit(long exit) {
        this.exit = exit;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getMajor() {
        return major;
    }

    public void setMajor(int major) {
        this.major = major;
    }

    public int getMinor() {
        return minor;
    }

    public void setMinor(int minor) {
        this.minor = minor;
    }
}
