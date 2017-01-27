package com.idea.todo.activity;

import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.idea.todo.R;
import com.idea.todo.adapter.recycler.GroupsAdapter;
import com.idea.todo.db.Database;
import com.idea.todo.model.GroupInfo;
import com.idea.todo.model.GroupsDialogArgs;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GroupOptionsActivity extends BaseActivity implements OnClickListener{

    private Database mDatabase;
    private ArrayList<GroupInfo> mGroupInfoList;

    @BindView(R.id.recyclerGroups)
    RecyclerView recyclerGroups;

    @BindView(R.id.fabBase)
    FloatingActionMenu fabBase;

    @BindView(R.id.fabGroupAdd)
    FloatingActionButton fabGroupAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        ButterKnife.bind(this);
        init();
    }

    private void init() {
        fabGroupAdd.setOnClickListener(this);
        mDatabase = Database.getInstance(this);
        recyclerGroups.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateGroupsAdapter();
    }

    public void updateGroupsAdapter() {
        mGroupInfoList = getGroups();

        GroupsDialogArgs args = new GroupsDialogArgs(REQUEST_DIALOG_FRAG_GROUP_SHOW);
        args.setContext(this);
        recyclerGroups.setAdapter(new GroupsAdapter(null, mGroupInfoList, args));
    }

    private ArrayList<GroupInfo> getGroups() {
        try {
            return mDatabase.getGroups();
        } catch (Exception e) {
            e.printStackTrace();
            displayToast(R.string.databaseError);
            return null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDatabase != null) {
            mDatabase.close();
        }
    }

    protected void addGroup() {
        Builder builder = new Builder(this);
        final EditText inputNewGroup = new EditText(this);
        inputNewGroup.setSingleLine();
        FrameLayout layout = new FrameLayout(this);
        layout.setPadding(10, 5, 10, 5);
        layout.addView(inputNewGroup);
        builder.setView(layout);
        builder.setTitle(R.string.groupAddTitle);
        builder.setIcon(R.drawable.ic_group);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    mDatabase.createGroup(inputNewGroup.getText().toString().trim());
                    updateGroupsAdapter();
                } catch (Exception e) {
                    displayToast(R.string.groupCreateFailedMessage);
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        final Button btnOk = builder.show().getButton(-1);
        btnOk.setEnabled(false);
        inputNewGroup.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable) {
                String newGroupName = getInputTxt(inputNewGroup);
                boolean isValidGroupName = newGroupName.length() > 0 && !isGroupNameDuplicate(newGroupName);
                btnOk.setEnabled(isValidGroupName);
            }
        });
    }

    public boolean isGroupNameDuplicate(String newGroupName) {
        for (GroupInfo group : mGroupInfoList) {
            if (group.getGroupName().equalsIgnoreCase(newGroupName)) return true;
        }
        return false;
    }

    @Override
    public void onClick(View view) {
        if (view == fabGroupAdd){
            addGroup();
            fabBase.close(true);
        }
    }


}
