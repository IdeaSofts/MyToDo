package com.idea.todo.frag.status;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.idea.todo.R;
import com.idea.todo.activity.ToDoActivity;
import com.idea.todo.adapter.recycler.ToDosAdapter;
import com.idea.todo.db.Database;
import com.idea.todo.frag.BaseFrag;
import com.idea.todo.listener.OnClickGroupItem;
import com.idea.todo.model.GroupInfo;
import com.idea.todo.model.ToDoInfo;
import com.idea.todo.wrapper.SharedData;
import com.idea.todo.wrapper.alarm.Alarms;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public abstract class StatusBase extends BaseFrag implements
        OnItemSelectedListener,
        OnClickGroupItem {
    
    @BindView(R.id.mainAddItemButton)
    protected Button btnAddItem;

    @BindView(R.id.spinnerGroups)
    protected Spinner spinnerGroups;
    
    @BindView(R.id.mainMessageView)
    protected TextView mMessageView;

    @BindView(R.id.recyclerToDos)
    RecyclerView recyclerToDos;

    protected ArrayAdapter<GroupInfo> mGroupSpinnerAdapter;
    protected ArrayList<ToDoInfo> toDos;
    protected ToDosAdapter toDosAdapter;
    protected Context context;
    protected Database mDatabase;
    protected ArrayList<GroupInfo> groupInfoList;
    protected SharedPreferences mSharedPreferences;
    protected int mStatus;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabase = Database.getInstance(getActivity());
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_status_base, container, false);
        ButterKnife.bind(this, v);
        init();
        return v;
    }

    private void init() {
        recyclerToDos.setLayoutManager(new LinearLayoutManager(getActivity()));
        context = getActivity();
        spinnerGroups.setOnItemSelectedListener(this);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mDatabase != null) {
            mDatabase.close();
        }
    }

    public void onResume() {
        super.onResume();
        updateSpinnerGroups();
        updateTodDosAdapter();
    }

    public void updateTodDosAdapter() {
        try {
            getToDosFromDatabase();
            removeEmptyToDos();
            setToDos();
        }
        catch (Exception e) {
            e.printStackTrace();
            base().displayToast(R.string.databaseError);
            getActivity().finish();
        }
    }

    private void getToDosFromDatabase() {
        boolean isSortByDate = mSharedPreferences.getBoolean(PREFS_LIST_SORT_ORDER, true);
        long currentGroup = SharedData.INSTANCE.getCurrentGroup();

        if (currentGroup == GROUP_ID_ALL)
            toDos = mDatabase.getAllGroups(mStatus, isSortByDate);
        else
            toDos = mDatabase.getGroup(mStatus, isSortByDate, currentGroup);
    }

    private void removeEmptyToDos() {
        for (int i = 0; i < toDos.size() ; i++){
            ToDoInfo toDoInfo = toDos.get(i);
            if (toDoInfo.getDetail().length() == 0) {
                try {
                    mDatabase.deleteItem(toDoInfo.getId());
                    toDos.remove(i);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void setToDos() {
        toDosAdapter = new ToDosAdapter(this, toDos, mStatus);
        recyclerToDos.setAdapter(toDosAdapter);

        if (toDos.size() == 0) {
            mMessageView.setText(R.string.mainListEmpty);
            mMessageView.setVisibility(View.VISIBLE);
            return;
        }

        mMessageView.setVisibility(View.GONE);
    }

    protected void updateSpinnerGroups() {
        try {
            groupInfoList = mDatabase.getGroups();
            mGroupSpinnerAdapter = new ArrayAdapter<>(getActivity(), R.layout.group_spinner, groupInfoList);
            spinnerGroups.setAdapter(mGroupSpinnerAdapter);
            int pos = getGroupPosition(SharedData.INSTANCE.getCurrentGroup());
            if (pos == -1) {
                pos = 0;
            }
            spinnerGroups.setSelection(pos);
        } catch (Exception e) {
            e.printStackTrace();
            base().displayToast(R.string.databaseError);
            getActivity().finish();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        if (view == spinnerGroups){
            SharedData.INSTANCE.setCurrentGroup((groupInfoList.get(position)).getId());
            updateTodDosAdapter();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {}

    public int getGroupPosition(long id) {
        int count = groupInfoList.size();
        for (int pos = 0; pos < count; pos++) {
            if ((groupInfoList.get(pos)).getId() == id) {
                return pos;
            }
        }
        return -1;
    }

    public void deleteToDo(ToDoInfo toDoInfo) {
        try {
            mDatabase.deleteItem(toDoInfo.getId());
            if (toDoInfo.getAlarmId() != 0)
                Alarms.deleteAlarm(getActivity(), toDoInfo.getAlarmId());
            updateTodDosAdapter();
        } catch (Exception e) {
            e.printStackTrace();
            toDosAdapter.base().displayToast(R.string.itemDeleteFailedMessage);
        }
    }

    public void editToDo(long toDoId, int toDoStatus) {
        Intent intent = new Intent(getContext(), ToDoActivity.class);
        intent.putExtra(INTENT_KEY_TODO_ID, toDoId);
        intent.putExtra(INTENT_KEY_TODO_STATUS, toDoStatus);
        getContext().startActivity(intent);
    }

    public void setStatus(int mStatus) {
        this.mStatus = mStatus;
    }

    @Override
    public void onClickGroupItem(GroupInfo groupInfo) {
        SharedData.INSTANCE.setCurrentGroup(groupInfo.getId());
        updateTodDosAdapter();
    }
}
