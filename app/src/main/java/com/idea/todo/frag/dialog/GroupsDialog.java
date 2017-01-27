package com.idea.todo.frag.dialog;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.idea.todo.R;
import com.idea.todo.activity.ToDoActivity;
import com.idea.todo.adapter.recycler.GroupsAdapter;
import com.idea.todo.constants.C;
import com.idea.todo.db.Database;
import com.idea.todo.model.GroupInfo;
import com.idea.todo.model.GroupsDialogArgs;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by sha on 20/01/17.
 */

public class GroupsDialog extends DialogFragment implements
        C{

    private GroupsDialogArgs args;
    private Database mDatabase;
    @BindView(R.id.recyclerGroups)
    RecyclerView recyclerGroups;

    public static GroupsDialog newInstance(GroupsDialogArgs args) {
        GroupsDialog f = new GroupsDialog();
        f.setArgs(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabase = Database.getInstance(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_dialog_groups, container, false);
        ButterKnife.bind(this, v);
        init();
        return v;
    }

    private void init() {
        recyclerGroups.setLayoutManager(new LinearLayoutManager(getActivity()));
        final ArrayList<GroupInfo> groupInfoList = getGroups();
        recyclerGroups.setAdapter(new GroupsAdapter(this, groupInfoList, args));
    }

    private ArrayList<GroupInfo> getGroups() {
        try {
            return mDatabase.getGroups();
        } catch (Exception e) {
            e.printStackTrace();
            base().displayToast(R.string.databaseError);
            return null;
        }
    }

    private void setArgs(GroupsDialogArgs args) {
        this.args = args;
    }

    public ToDoActivity base(){
        return (ToDoActivity) getActivity();
    }

}