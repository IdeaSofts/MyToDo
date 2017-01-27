package com.idea.todo.frag.dialog;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.idea.todo.R;
import com.idea.todo.activity.BaseActivity;
import com.idea.todo.activity.GroupOptionsActivity;
import com.idea.todo.constants.C;
import com.idea.todo.db.Database;
import com.idea.todo.listener.OnClickAlertDialog;
import com.idea.todo.model.AlertDialogArgs;
import com.idea.todo.model.GroupInfo;
import com.idea.todo.wrapper.SharedData;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by sha on 20/01/17.
 */

public class GroupsOptionsDialog extends DialogFragment implements
        C,
        View.OnClickListener,
        OnClickAlertDialog{

    private GroupInfo groupInfo;
    private Database mDatabase;


    @BindView(R.id.btnEdit)
    Button btnEdit;

    @BindView(R.id.btnDelete)
    Button btnDelete;

    public static GroupsOptionsDialog newInstance(GroupInfo groupInfo) {
        GroupsOptionsDialog f = new GroupsOptionsDialog();
        f.setGroupInfo(groupInfo);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabase = Database.getInstance(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_dialog_groups_options, container, false);
        ButterKnife.bind(this, v);
        init();
        return v;
    }

    private void init() {
        btnEdit.setOnClickListener(this);
        btnDelete.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        if (v == btnEdit){
            editGroup();
        }

        else if (v == btnDelete){
            confirmDeleteGroup();
        }
    }

    protected void confirmDeleteGroup() {
        AlertDialogArgs args = new AlertDialogArgs(
                this,
                getString(R.string.itemDeleteTitle),
                REQUEST_ALERT_DIALOG_GROUP_DELETE);
        ((BaseActivity)getActivity()).alertDialog(args);
    }

    @Override
    public void onClickAlertDialog(AlertDialogArgs args) {
        switch (args.getRequest()){
            case REQUEST_ALERT_DIALOG_GROUP_DELETE:
                if (args.isPositive())
                    deleteGroup();
                break;
        }
    }

    private void deleteGroup() {
        try {
            mDatabase.deleteGroup(groupInfo.getId());
            if (SharedData.INSTANCE.getCurrentGroup() == groupInfo.getId()) {
                SharedData.INSTANCE.setCurrentGroup(GROUP_ID_ALL);
            }
            base().updateGroupsAdapter();
            dismiss();
        } catch (Exception e) {
            e.printStackTrace();
            base().displayToast(R.string.groupDeleteFailedMessage);
            dismiss();
        }
    }

    private void editGroup() {
        final long groupId = groupInfo.getId();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final EditText input = new EditText(getActivity());
        input.setSingleLine();
        input.setText(groupInfo.getGroupName());
        FrameLayout layout = new FrameLayout(getActivity());
        layout.setPadding(10, 5, 10, 5);
        layout.addView(input);
        builder.setView(layout);
        builder.setTitle(R.string.groupEditTitle);
        builder.setIcon(R.drawable.ic_group);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    mDatabase.setGroupName(groupId, input.getText().toString().trim());
                    base().updateGroupsAdapter();
                    dismiss();
                } catch (Exception e) {
                    dismiss();
                    base().displayToast(R.string.groupUpdateFailedMessage);
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        final Button okButton = builder.show().getButton(-1);
        okButton.setEnabled(false);
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                String newName = base().getInputTxt(input);
                boolean z = newName.length() > 0 && !base().isGroupNameDuplicate(newName);
                okButton.setEnabled(z);
            }
        });
    }

    public void setGroupInfo(GroupInfo groupInfo) {
        this.groupInfo = groupInfo;
    }

    public GroupOptionsActivity base(){
        return (GroupOptionsActivity) getActivity();
    }
}