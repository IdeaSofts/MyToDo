package com.idea.todo.activity;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.idea.todo.R;
import com.idea.todo.activity.alarm.SetAlarmActivity;
import com.idea.todo.db.Database;
import com.idea.todo.frag.dialog.GroupsDialog;
import com.idea.todo.listener.OnClickGroupItem;
import com.idea.todo.model.GroupInfo;
import com.idea.todo.model.GroupsDialogArgs;
import com.idea.todo.model.ToDoInfo;
import com.idea.todo.wrapper.DateWrapper;
import com.idea.todo.wrapper.alarm.Alarms;

import java.util.Calendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ToDoActivity extends BaseActivity implements
        View.OnClickListener,
        OnClickGroupItem {

    private Database mDatabase;
    private ToDoInfo toDoInfo;
    private long toDoId;
    private int toDoStatus;

    @BindView(R.id.tvToDoDate)
    TextView tvToDoDate;
    @BindView(R.id.inputToDoDetail)
    EditText inputToDoDetail;
    @BindView(R.id.inputToDoTitle)
    EditText inputToDoTitle;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.btnPickTime)
    Button btnPickTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo);
        ButterKnife.bind(this);
        getIntentData();
        init();
        initToDo();
    }

    private void getIntentData() {
        Bundle extras = getIntent().getExtras();
        toDoId = extras.getLong(INTENT_KEY_TODO_ID);
        toDoStatus = extras.getInt(INTENT_KEY_TODO_STATUS);
    }

    private boolean isToDoNew() {
        return toDoId == TODO_IS_NEW;
    }

    private void init() {
        setSupportActionBar(toolbar);
        mDatabase = Database.getInstance(this);

        btnPickTime.setOnClickListener(this);
        if (isToDoNew())
            btnPickTime.setText(R.string.set_alarm);
        else
            btnPickTime.setText(R.string.update_alarm);
    }

    private void initToDo() {
        try {
            toDoInfo = isToDoNew() ? new ToDoInfo() : mDatabase.getToDo(toDoId);
            inputToDoDetail.setText(toDoInfo.getDetail());
            inputToDoTitle.setText(toDoInfo.getTitle());
            setItemTitle();
            if (!isToDoNew()) setDateSummary();
        } catch (Exception e) {
            e.printStackTrace();
            displayToast(R.string.databaseError);
            finish();
        }
    }

    private void testData(ToDoInfo toDo) {
        if (toDo == null) return;

        Log.e("testData", "****************");
        Log.e("testData", "toDo.getDetail() = " + toDo.getDetail());
        Log.e("testData", "toDo.getDetail() = " + toDo.getTitle());
        Log.e("testData", "****************");
    }

    @Override
    public void onClick(View view) {
        if (view == btnPickTime){
            addAlarm();
        }
    }

    private void addAlarm() {
        int alarmId = toDoInfo.getAlarmId();
        com.idea.todo.wrapper.Log.e("addAlarm : 1 alarmId = " + alarmId);
        /**
         * If alarmId == 0 there is no old alarm for
         * this toDo so we must create a new alarm
         * and save it in db.
         */
        if (alarmId == 0){
            Uri uri = Alarms.addAlarm(getContentResolver());
            // TODO: Create new alarm _after_ SetAlarm so the user has the
            // chance to cancel alarm creation.
            String segment = uri.getPathSegments().get(1);
            alarmId = Integer.parseInt(segment);
            if (com.idea.todo.wrapper.Log.LOGV) {
                com.idea.todo.wrapper.Log.v("In addAlarm, new alarm id = " + alarmId);
            }
//            saveAlarmIdInToDosTable(alarmId);
            toDoInfo.setAlarmId(alarmId);
        }
        com.idea.todo.wrapper.Log.e("addAlarm : 2 alarmId = " + alarmId);

        Intent intent = new Intent(this, SetAlarmActivity.class);
        intent.putExtra(Alarms.ALARM_ID, alarmId);
        intent.putExtra(INTENT_KEY_TODO_DATE, toDoInfo.getDate());
        startActivityForResult(intent, PREFS_ALARM_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        com.idea.todo.wrapper.Log.e("resultCode = " + resultCode);
        com.idea.todo.wrapper.Log.e("requestCode = " + requestCode);
        switch (resultCode){
            case Activity.RESULT_OK :
                switch (requestCode){
                    case PREFS_ALARM_REQUEST_CODE :
                        long date = intent.getLongExtra(INTENT_KEY_TODO_DATE, 0);
                        com.idea.todo.wrapper.Log.e("onActivityResult date = " + date);
                        toDoInfo.setDate(date);
                        toDoInfo.setAutoMoved(false);
                        setDateSummary();
                        break;
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDatabase != null) {
            mDatabase.close();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_todo, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.itemMenuClearDate).setEnabled(toDoInfo.getDate() > 0);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.itemMenuSetGroup:
                setToDoGroup();
                break;
            case R.id.itemMenuSetAlarm:
                addAlarm();
                break;
            case R.id.itemMenuClearDate:
                clearToDoDate();
                break;
            case R.id.menuSave:
                finish();
                break;
        }
        return true;
    }

    private void setToDoGroup() {
        GroupsDialogArgs groupsDialogArgs = new GroupsDialogArgs(REQUEST_DIALOG_FRAG_GROUP_SET);

        GroupsDialog groupsDialog = GroupsDialog.newInstance(groupsDialogArgs);
        groupsDialog.show(getSupportFragmentManager(), "groupsDialog");
    }

    @Override
    public void onClickGroupItem(GroupInfo groupInfo) {
        if (toDoInfo.getGroup() == groupInfo.getId()){

            String msg = String.format(
                    Locale.US,
                    getString(R.string.group_already_set),
                    groupInfo.getGroupName());

            displayToast(msg);
            return;
        }

        toDoInfo.setGroup(groupInfo.getId());
        setItemTitle();

        String msg = String.format(
                Locale.US,
                getString(R.string.group_success_set),
                groupInfo.getGroupName());
        displayToast(msg);

    }

    private void setItemTitle() {
        String title;

        if (isToDoNew()) title = getString(R.string.itemAddTitle);
        else title = getString(R.string.itemEditTitle);

        if (toDoInfo.getGroup() == GROUP_ID_ALL) {
            setTitle(getString(R.string.itemAllGroupsTitle, title));
            return;
        }

        String name = getString(R.string.unknown);
        try {
            name = mDatabase.getGroup(toDoInfo.getGroup()).getGroupName();
        }
        catch (Exception e) {
            e.printStackTrace();
            displayToast(R.string.databaseError);
        }
        setTitle(getString(R.string.itemGroupTitle, title, name));
    }

    public void setDateSummary() {
        long date = toDoInfo.getDate();
        if (date > 0) {
            tvToDoDate.setVisibility(View.VISIBLE);
            String txt = DateWrapper.dateSummary(getApplicationContext(), date);
            com.idea.todo.wrapper.Log.e("txt = " + txt);

            tvToDoDate.setText(txt);
            tvToDoDate.setTextColor(DateWrapper.getDateColor(date, this));
            return;
        }
        tvToDoDate.setVisibility(View.GONE);
    }

    private void clearToDoDate() {
        Builder builder = new Builder(this);
        builder.setTitle(R.string.itemClearDate);
        builder.setMessage(R.string.itemClearDateMessage);
        builder.setPositiveButton(R.string.ok, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                toDoInfo.setDate(0);
                setDateSummary();
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.create().show();
    }

    @Override
    public void onPause() {
        super.onPause();
        saveToDo();
    }

    private void saveToDo() {
        Log.e("saveToDo", "isToDoNew() = " + isToDoNew());
        Log.e("saveToDo", "toDoId = " + toDoId);
        try {
            toDoInfo.setDetail(getInputTxt(inputToDoDetail));
            toDoInfo.setTitle(getInputTxt(inputToDoTitle));
            if (isToDoNew()) {
                toDoInfo.setStatus(toDoStatus);
                toDoId = mDatabase.create(ToDoInfo.TABLE_NAME, toDoInfo.getValues());
                Log.e("saveToDo", "isToDoNew() = true, toDoId = " + toDoId);

                getIntent().putExtra(INTENT_KEY_TODO_ID, toDoId);
                return;
            }
            toDoInfo.setDateModified(Calendar.getInstance().getTimeInMillis());
            mDatabase.update(ToDoInfo.TABLE_NAME, toDoInfo.getValues(), "_id = ?", new String[]{String.valueOf(toDoId)});
            Log.e("saveToDo", "isToDoNew() = false, toDoId = " + toDoId);
        } catch (Exception e) {
            e.printStackTrace();
            displayToast(R.string.itemSaveFailedMessage);
        }
    }
}
