package com.idea.todo.model;

public class ControlInfo {
    public static final String LONG_VAL = "long_val";
    public static final String NAME = "name";
    public static final String TABLE_NAME = "control";
    public static final String TEXT_VAL = "text_val";
    public static final String _ID = "_id";

    public static String getCreateStatement() {
        return String.format("create table %s (" +
                " %s integer primary key autoincrement, " +
                "%s text," +
                " %s integer," +
                " %s text);",
                TABLE_NAME,
                _ID,
                NAME,
                LONG_VAL,
                TEXT_VAL);
    }
}
