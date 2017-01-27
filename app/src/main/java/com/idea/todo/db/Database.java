package com.idea.todo.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.SparseIntArray;

import com.idea.todo.R;
import com.idea.todo.constants.C;
import com.idea.todo.model.ControlInfo;
import com.idea.todo.model.GroupInfo;
import com.idea.todo.model.ToDoInfo;

import java.util.ArrayList;
import java.util.List;

public class Database extends SQLiteOpenHelper implements C{
    public static final String DB_NAME = "todo_list";
    public static final int DB_VERSION = 1;
    private Context mContext;
    private static Database instance;

    public Database(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        mContext = context;
    }

    public static Database getInstance(Context context){
        if (instance == null) instance = new Database(context);
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(ToDoInfo.getCreateStatement());
        db.execSQL(GroupInfo.getCreateStatement());
        db.execSQL(ControlInfo.getCreateStatement());
        String allGroups = mContext.getString(R.string.groupNameAllGroups);
        db.execSQL(String.format("insert into %s (" +
                "%s) " +
                "values ('%s')",
                GroupInfo.TABLE_NAME,
                GroupInfo.GROUP_NAME,
                allGroups));
        db.execSQL(String.format("insert into %s (" +
                " %s," +
                " %s) " +
                "values ('%s', 1)",
                ControlInfo.TABLE_NAME,
                ControlInfo.NAME,
                ControlInfo.LONG_VAL,
                C.KEY_CURRENT_GROUP));
    }

    @Override
    public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
    }

    public long create(String tableName, ContentValues values) {
        return getWritableDatabase().insert(tableName, null, values);
    }

    public int update(String tableName, ContentValues values, String where, String[] whereValues) {
        return getWritableDatabase().update(tableName, values, where, whereValues);
    }

    public int delete(String tableName, String where, String[] whereValues) {
        return getWritableDatabase().delete(tableName, where, whereValues);
    }

    public void writeControlLongValue(String name, long val) {
        ContentValues values = new ContentValues();
        values.put(ControlInfo.LONG_VAL, val);

        update(ControlInfo.TABLE_NAME, values, "name = ?", new String[]{name});
    }

    public long readControlLongValue(String name, long defVal) {
        String sql = String.format("select %s " +
                "from %s " +
                "where %s = ?",
                ControlInfo.LONG_VAL,
                ControlInfo.TABLE_NAME,
                ControlInfo.NAME);
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            String[] strArr = new String[1];
            strArr[0] = name;
            cursor = db.rawQuery(sql, strArr);
            if (cursor.moveToFirst()) {
                defVal = cursor.getLong(0);
                if (cursor != null) {
                    cursor.close();
                }
            }
            else if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return defVal;
    }


    private GroupInfo createGroupInfo(Cursor cursor) {
        return new GroupInfo(
                cursor.getLong(0),
                cursor.getString(1)
        );

    }

    public void createGroup(String name) {
        ContentValues values = new ContentValues();
        values.put(GroupInfo.GROUP_NAME, name);
        create(GroupInfo.TABLE_NAME, values);
    }

    public void updateGroup(long ToDoId, long groupId) {
        ContentValues values = new ContentValues();
        values.put(ToDoInfo.TODO_GROUP, groupId);
        update(
                ToDoInfo.TABLE_NAME,
                values, ToDoInfo.TODO_GROUP + " = ?",
                new String[]{String.valueOf(ToDoId)
                });
    }

    public void deleteGroup(long id) {
        ContentValues values = new ContentValues();
        values.put(ToDoInfo.TODO_GROUP, 1);
        String[] strArr = new String[1];
        strArr[0] = String.valueOf(id);
        update(ToDoInfo.TABLE_NAME, values, ToDoInfo.TODO_GROUP + " = ?", strArr);
        strArr = new String[1];
        strArr[0] = String.valueOf(id);
        delete(GroupInfo.TABLE_NAME, "_id = ?", strArr);
    }

    public void setGroupName(long id, String name) {
        ContentValues values = new ContentValues();
        values.put(GroupInfo.GROUP_NAME, name);
        String[] strArr = new String[1];
        strArr[0] = String.valueOf(id);
        update(GroupInfo.TABLE_NAME, values, "_id = ?", strArr);
    }

    public ArrayList<ToDoInfo> getAllGroups(int status, boolean sortByItemDate) {
        String orderBy = sortByItemDate ? ToDoInfo.TODO_DATE : ToDoInfo.DATE_CREATED;
        String select = String.format("select * from %s " +
                "where %s = ?" +
                " order by %s",
                ToDoInfo.TABLE_NAME,
                ToDoInfo.TODO_STATUS,
                orderBy);

        Cursor cursor = null;
        ArrayList<ToDoInfo> list = new ArrayList<>();

        try {
            cursor = getReadableDatabase().rawQuery(select, new String[]{String.valueOf(status)});

            while (cursor.moveToNext()) {
                list.add(createToDoInfo(cursor));
            }

            return list;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private ToDoInfo createToDoInfo(Cursor cursor) {
        return new ToDoInfo(
                cursor.getLong(0),
                cursor.getString(1),
                cursor.getString(2),
                cursor.getLong(3),
                cursor.getInt(4),
                cursor.getLong(5),
                cursor.getLong(6),
                cursor.getLong(7),
                cursor.getInt(8) != 0,
                cursor.getInt(9));
    }

    public List<Long> getMoveableItems() {
        String select = String.format("select %s " +
                "from %s " +
                "where %s = %s " +
                "and %s > 0 " +
                "and %s = 0",
                ToDoInfo._ID,
                ToDoInfo.TABLE_NAME,
                ToDoInfo.TODO_STATUS,
                1,
                ToDoInfo.TODO_DATE,
                ToDoInfo.AUTO_MOVED);
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        List<Long> list = new ArrayList<>();
        try {
            cursor = db.rawQuery(select, new String[0]);
            while (cursor.moveToNext()) {
                list.add(cursor.getLong(0));
            }
            return list;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public ToDoInfo getToDo(long id) {
        String sql = String.format("select * from %s " +
                "where %s = ?",
                ToDoInfo.TABLE_NAME,
                ToDoInfo._ID);
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        ToDoInfo task = null;
        try {
            String[] strArr = new String[1];
            strArr[0] = String.valueOf(id);
            cursor = db.rawQuery(sql, strArr);
            if (cursor.moveToFirst()) {
                task = createToDoInfo(cursor);
            }
            if (cursor != null) {
                cursor.close();
            }
            return task;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    public void deleteItem(long id) {
        delete(ToDoInfo.TABLE_NAME, "_id = ?", new String[]{String.valueOf(id)});
    }

    public void deleteToDoGroup(long groupId, int status) {
        if (groupId == GROUP_ID_ALL) {
            delete(ToDoInfo.TABLE_NAME, ToDoInfo.TODO_STATUS + " = ?", new String[]{String.valueOf(status)});
            return;
        }

        String where = String.format("%s = ? and %s = ?", ToDoInfo.TODO_GROUP, ToDoInfo.TODO_STATUS);
        delete(ToDoInfo.TABLE_NAME, where, new String[]{String.valueOf(groupId), String.valueOf(status)});
    }

    public void setToDoStatus(long id, int status) {
        ContentValues values = new ContentValues();
        values.put(ToDoInfo.TODO_STATUS, status);
        update(ToDoInfo.TABLE_NAME, values, "_id = ?", new String[]{String.valueOf(id)});
    }

    public void setItemMoved(long id) {
        ContentValues values = new ContentValues();
        values.put(ToDoInfo.TODO_STATUS, 0);
        values.put(ToDoInfo.AUTO_MOVED, 1);
        String[] strArr = new String[1];
        strArr[0] = String.valueOf(id);
        update(ToDoInfo.TABLE_NAME, values, "_id = ?", strArr);
    }

    public void importData(ArrayList<GroupInfo> groupInfoList, ArrayList<ToDoInfo> toDoInfoList) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        db.delete(GroupInfo.TABLE_NAME, "_id > 1", null);
        db.delete(ToDoInfo.TABLE_NAME, null, null);
        SparseIntArray oldNewIds = new SparseIntArray(groupInfoList.size() + 1);
        oldNewIds.append(1, 1);

        for (GroupInfo groupInfo: groupInfoList){
            oldNewIds.append((int) groupInfo.getId(), (int) db.insert(GroupInfo.TABLE_NAME, null, groupInfo.getValues()));
        }

        for (ToDoInfo toDoInfo : toDoInfoList){
            toDoInfo.setGroup((long) oldNewIds.get((int) toDoInfo.getGroup()));
            db.insert(ToDoInfo.TABLE_NAME, null, toDoInfo.getValues());
        }

        db.setTransactionSuccessful();
    }

    public ArrayList<ToDoInfo> getGroup(int status) {

        String select = String.format("select * from %s " +
                        "where %s = ? ",
                ToDoInfo.TABLE_NAME,
                ToDoInfo.TODO_STATUS);
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        ArrayList<ToDoInfo> list = new ArrayList<>();
        try {
            cursor = db.rawQuery(select, new String[]{String.valueOf(status)});
            while (cursor.moveToNext()) {
                list.add(createToDoInfo(cursor));
            }
            return list;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public ArrayList<ToDoInfo> getGroup(int status, boolean sortByItemDate, long groupId) {
        String orderBy = sortByItemDate ? ToDoInfo.TODO_DATE : ToDoInfo.DATE_CREATED;
        String select = String.format("select * from %s " +
                        "where %s = ? " +
                        "and (%s = ?) " +
                        "order by %s",
                ToDoInfo.TABLE_NAME,
                ToDoInfo.TODO_STATUS,
                ToDoInfo.TODO_GROUP,
                orderBy);
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        ArrayList<ToDoInfo> list = new ArrayList<>();
        try {
            cursor = db.rawQuery(select, new String[]{String.valueOf(status), String.valueOf(groupId)});
            while (cursor.moveToNext()) {
                list.add(createToDoInfo(cursor));
            }
            return list;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public GroupInfo getGroup(long id) {
        String sql = String.format("select * from %s " +
                        "where %s = ?",
                GroupInfo.TABLE_NAME,
                ToDoInfo._ID);
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        GroupInfo groupInfo = null;
        try {
            String[] strArr = new String[1];
            strArr[0] = String.valueOf(id);
            cursor = db.rawQuery(sql, strArr);
            if (cursor == null) return null;
            if (cursor.moveToFirst()) {
                groupInfo = createGroupInfo(cursor);
            }
            cursor.close();
            return groupInfo;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    public ArrayList<GroupInfo> getGroups() {
        String select = String.format("select * from %s", GroupInfo.TABLE_NAME);
        Cursor cursor = null;
        ArrayList<GroupInfo> list = new ArrayList<>();
        try {
            cursor = getReadableDatabase().rawQuery(select, null);
            while (cursor.moveToNext()) {
                list.add(createGroupInfo(cursor));
            }
            return list;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
