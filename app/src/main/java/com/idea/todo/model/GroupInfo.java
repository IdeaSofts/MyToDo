package com.idea.todo.model;

import android.content.ContentValues;

public class GroupInfo {
    public static final String GROUP_NAME = "group_name";
    public static final String TABLE_NAME = "groups";
    public static final String _ID = "_id";
    private String groupName;
    private long id;

    public static String getCreateStatement() {
        return String.format("create table %s (" +
                " %s integer primary key autoincrement," +
                " %s text);",
                TABLE_NAME,
                _ID,
                GROUP_NAME);
    }

    public GroupInfo() {}


    public GroupInfo(
            long id,
            String groupName) {
        this.id = id;
        this.groupName = groupName;
    }

    public ContentValues getValues() {
        ContentValues values = new ContentValues();
        values.put(GROUP_NAME, getGroupName());
        return values;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getGroupName() {
        return this.groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String toString() {
        return getGroupName();
    }
}
