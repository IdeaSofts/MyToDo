package com.idea.todo.model;

import android.content.ContentValues;

import com.idea.todo.wrapper.SharedData;

import java.util.Calendar;

public class ToDoInfo {
    public static final String AUTO_MOVED = "auto_moved";
    public static final String ALARM_ID = "alarmId";
    public static final String DATE_CREATED = "created_date";
    public static final String TODO_DATE = "todo_date";
    public static final String TODO_DETAIL = "todo_detail";
    public static final String TODO_TITLE = "todo_title";
    public static final String TODO_GROUP = "todo_group";
    public static final String TODO_STATUS = "todo_status";
    public static final String DATE_MODIFIED = "modified_date";
    public static final String TABLE_NAME = "toDos";
    public static final String _ID = "_id";
    private boolean isAutoMoved;
    private long dateCreated;
    private long id;
    private int alarmId;
    private long date;
    private String
            detail,
            title;
    private long group;
    private int status;
    private long dateModified;

    public static String getCreateStatement() {
        return String.format("create table %s (" +
                " %s integer primary key autoincrement," +
                " %s text, " +
                " %s text, " +
                " %s integer," +
                " %s integer," +
                " %s integer," +
                " %s integer," +
                " %s integer, " +
                " %s integer, " +
                " %s integer);",
                TABLE_NAME,
                _ID,
                TODO_DETAIL,
                TODO_TITLE,
                TODO_GROUP,
                TODO_STATUS,
                TODO_DATE,
                DATE_CREATED,
                DATE_MODIFIED,
                AUTO_MOVED,
                ALARM_ID);
    }

    public ToDoInfo(
            long id,
            String detail,
            String title,
            long group,
            int status,
            long date,
            long dateCreated,
            long dateModified,
            boolean isAutoMoved,
            int alarmId
            ) {
        this.id = id;
        this.detail = detail;
        this.title = title;
        this.group = group;
        this.status = status;
        this.date = date;
        this.dateCreated = dateCreated;
        this.dateModified = dateModified;
        this.isAutoMoved = isAutoMoved;
        this.alarmId = alarmId;
    }

    public ToDoInfo() {
        setGroup(SharedData.INSTANCE.getCurrentGroup());
        setStatus(0);
        setDate(0);
        setDateCreated(Calendar.getInstance().getTimeInMillis());
        setDateModified(0);
        setAutoMoved(false);
    }

    public ContentValues getValues() {
        ContentValues values = new ContentValues();
        values.put(TODO_DETAIL,detail);
        values.put(TODO_TITLE, title);
        values.put(TODO_GROUP, group);
        values.put(TODO_STATUS, status);
        values.put(TODO_DATE, date);
        values.put(DATE_CREATED, dateCreated);
        values.put(DATE_MODIFIED, dateModified);
        values.put(AUTO_MOVED, isAutoMoved() ? 1 : 0);
        values.put(ALARM_ID, alarmId);
        return values;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public long getGroup() {
        return group;
    }

    public void setGroup(long group) {
        this.group = group;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public long getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(long dateCreated) {
        this.dateCreated = dateCreated;
    }

    public long getDateModified() {
        return dateModified;
    }

    public void setDateModified(long dateModified) {
        this.dateModified = dateModified;
    }

    public boolean isAutoMoved() {
        return isAutoMoved;
    }

    public void setAutoMoved(boolean autoMoved) {
        this.isAutoMoved = autoMoved;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getAlarmId() {
        return alarmId;
    }

    public void setAlarmId(int alarmId) {
        this.alarmId = alarmId;
    }
}
