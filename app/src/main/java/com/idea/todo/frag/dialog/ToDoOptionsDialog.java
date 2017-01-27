package com.idea.todo.frag.dialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.idea.todo.R;
import com.idea.todo.activity.BaseActivity;
import com.idea.todo.constants.C;
import com.idea.todo.db.Database;
import com.idea.todo.frag.status.StatusBase;
import com.idea.todo.listener.OnClickAlertDialog;
import com.idea.todo.listener.OnClickGroupItem;
import com.idea.todo.model.AlertDialogArgs;
import com.idea.todo.model.GroupInfo;
import com.idea.todo.model.GroupsDialogArgs;
import com.idea.todo.wrapper.SharedData;
import com.idea.todo.model.ToDoInfo;
import com.idea.todo.wrapper.alarm.Alarms;

import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by sha on 20/01/17.
 */

public class ToDoOptionsDialog extends DialogFragment implements
        C,
        View.OnClickListener,
        OnClickAlertDialog,
        OnClickGroupItem {
    private  int toDoStatus;
    private ToDoInfo toDoInfo;

    private Database mDatabase;
    private StatusBase statusBase;

    @Nullable
    @BindView(R.id.btnNow)
    Button btnNow;
    @Nullable
    @BindView(R.id.btnLater)
    Button btnLater;
    @Nullable
    @BindView(R.id.btnDone)
    Button btnDone;
    @Nullable
    @BindView(R.id.btnEdit)
    Button btnEdit;
    @Nullable
    @BindView(R.id.btnDelete)
    Button btnDelete;
    @Nullable
    @BindView(R.id.btnDeleteAll)
    Button btnDeleteAll;
    @Nullable
    @BindView(R.id.btnGroup)
    Button btnGroup;
    /**
     * Create a new instance of ToDoOptionsDialog, providing "num"
     * as an argument.
     */
    public static ToDoOptionsDialog newInstance(
            int toDoStatus,
            ToDoInfo ToDoInfo,
            StatusBase statusBase) {
        
        ToDoOptionsDialog f = new ToDoOptionsDialog();

        Bundle args = new Bundle();
        args.putInt(INTENT_KEY_TODO_STATUS, toDoStatus);
        f.setArguments(args);
        f.setToDoInfo(ToDoInfo);
        f.setStatusBase(statusBase);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabase = Database.getInstance(getContext());
        toDoStatus = getArguments().getInt(INTENT_KEY_TODO_STATUS);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = null;

        switch (toDoStatus){

            case TODO_STATUS_NOW :
                v = inflater.inflate(R.layout.fragment_dialog_todo_now, container, false);
                break;

             case TODO_STATUS_LATER :
                 v = inflater.inflate(R.layout.fragment_dialog_todo_later, container, false);
                break;

             case TODO_STATUS_DONE :
                 v = inflater.inflate(R.layout.fragment_dialog_todo_done, container, false);
                break;

        }
        ButterKnife.bind(this, v);
        init();
        return v;
    }

    private void init() {
        if (btnNow != null) btnNow.setOnClickListener(this);
        if (btnLater != null) btnLater.setOnClickListener(this);
        if (btnDone != null) btnDone.setOnClickListener(this);
        if (btnEdit != null) btnEdit.setOnClickListener(this);
        if (btnDelete != null) btnDelete.setOnClickListener(this);
        if (btnDeleteAll != null) btnDeleteAll.setOnClickListener(this);
        if (btnGroup != null) btnGroup.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        if (v == btnNow){
            updateStatus(TODO_STATUS_NOW);
            dismiss();
            statusBase.updateTodDosAdapter();
        }

        else if (v == btnLater){
            updateStatus(TODO_STATUS_LATER);
            dismiss();
            statusBase.updateTodDosAdapter();
        }

        else if (v == btnDone){
            updateStatus(TODO_STATUS_DONE);
            dismiss();
            statusBase.updateTodDosAdapter();
        }

        else if (v == btnEdit){
            statusBase.editToDo(toDoInfo.getId(), toDoStatus);
            dismiss();
            statusBase.updateTodDosAdapter();
        }

        else if (v == btnDelete){
            confirmDeleteToDo();
        }

        else if (v == btnDeleteAll){
            confirmDeleteToDoGroup();
        }

        else if (v == btnGroup){
            setToDoGroup();
            dismiss();
            statusBase.updateTodDosAdapter();
        }


    }

    private void updateStatus(int status) {
        mDatabase.setToDoStatus(toDoInfo.getId(), status);
    }

    private void setToDoGroup() {
        GroupsDialogArgs groupsDialogArgs = new GroupsDialogArgs(REQUEST_DIALOG_FRAG_GROUP_SET);

        GroupsDialog groupsDialog = GroupsDialog.newInstance(groupsDialogArgs);
        groupsDialog.show(base().getSupportFragmentManager(), "groupsDialog");
    }

    @Override
    public void onClickGroupItem(GroupInfo groupInfo) {

        if (toDoInfo.getGroup() == groupInfo.getId()){

            String msg = String.format(
                    Locale.US,
                    getString(R.string.group_already_set),
                    groupInfo.getGroupName());

            base().displayToast(msg);
            return;
        }
        String msg = String.format(
                Locale.US,
                getString(R.string.group_success_set),
                groupInfo.getGroupName());
        base().displayToast(msg);

        mDatabase.updateGroup(toDoInfo.getId(), groupInfo.getId());
        dismiss();
    }

    protected void confirmDeleteToDo() {
        AlertDialogArgs args = new AlertDialogArgs(
                this,
                getString(R.string.itemDeleteMessage),
                REQUEST_ALERT_DIALOG_TODO_DELETE);
        args.setToDoId(toDoInfo.getId());
        ((BaseActivity)getActivity()).alertDialog(args);
    }

    protected void confirmDeleteToDoGroup() {
        AlertDialogArgs args = new AlertDialogArgs(
                this,
                getString(R.string.itemDeleteAllMessage),
                REQUEST_ALERT_DIALOG_TODO_DELETE_ALL);
        args.setToDoGroup(SharedData.INSTANCE.getCurrentGroup());
        args.setToDoStatus(toDoStatus);
        ((BaseActivity)getActivity()).alertDialog(args);
    }

    @Override
    public void onClickAlertDialog(AlertDialogArgs args) {
        switch (args.getRequest()){

            case REQUEST_ALERT_DIALOG_TODO_DELETE:
                if (args.isPositive())
                    statusBase.deleteToDo(toDoInfo);

                dismiss();
                statusBase.updateTodDosAdapter();
                break;

            case REQUEST_ALERT_DIALOG_TODO_DELETE_ALL:
                if (args.isPositive())
                    deleteToDoGroup();
                break;
        }
    }

    protected void deleteToDoGroup() {
        try {
            long groupId = SharedData.INSTANCE.getCurrentGroup();
            deleteAlarmsFirst(groupId);
            mDatabase.deleteToDoGroup(groupId, toDoStatus);

            dismiss();
            statusBase.updateTodDosAdapter();
        } catch (Exception e) {
            e.printStackTrace();
            base().displayToast(R.string.itemsDeleteFailedMessage);
        }
    }

    private void deleteAlarmsFirst(long groupId) {
        ArrayList<ToDoInfo> toDos;
        if (groupId == GROUP_ID_ALL)
            toDos = mDatabase.getGroup(toDoStatus);
        else
            toDos =  mDatabase.getGroup(toDoStatus, false, groupId);
        for (ToDoInfo toDo : toDos){
            if (toDo.getAlarmId() != 0)
                Alarms.deleteAlarm(getActivity(), toDo.getAlarmId());
        }
    }

    public void setToDoInfo(ToDoInfo toDoInfo) {
        this.toDoInfo = toDoInfo;
    }

    public void setStatusBase(StatusBase statusBase) {
        this.statusBase = statusBase;
    }

    public BaseActivity base(){
        return (BaseActivity) getActivity();
    }
}