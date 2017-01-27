package com.idea.todo.adapter.recycler;

import android.content.Context;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.idea.todo.R;
import com.idea.todo.activity.GroupOptionsActivity;
import com.idea.todo.frag.dialog.GroupsDialog;
import com.idea.todo.frag.dialog.GroupsOptionsDialog;
import com.idea.todo.listener.OnClickGroupItem;
import com.idea.todo.model.GroupInfo;
import com.idea.todo.model.GroupsDialogArgs;

import java.util.ArrayList;

public class GroupsAdapter extends BaseRecyclerAdapter<GroupsAdapter.ViewHolder>{
    private GroupsDialog groupsDialog;
    private ArrayList<GroupInfo> groupInfoList;
    private GroupsDialogArgs args;

    public GroupsAdapter(
            GroupsDialog groupsDialog,
            ArrayList<GroupInfo> groupInfoList,
            GroupsDialogArgs args) {
        this.groupInfoList = groupInfoList;
        this.groupsDialog = groupsDialog;
        this.args = args;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        View v;
        TextView tvGroup;
        ViewHolder(View v) {
            super(v);
            this.v = v;
            v.setOnClickListener(this);
            tvGroup = (TextView) v.findViewById(R.id.tvGroup);
        }

        @Override
        public void onClick(View view) {
            if (view == v) {
                handleRequest();
            }
        }

        private void handleRequest() {
            GroupInfo groupInfo = groupInfoList.get(getLayoutPosition());
            switch (args.getRequest()){
                case REQUEST_DIALOG_FRAG_GROUP_SET :
                    ((OnClickGroupItem)groupsDialog.getActivity()).onClickGroupItem(groupInfo);
                    groupsDialog.dismiss();
                    break;

               case REQUEST_DIALOG_FRAG_GROUP_SELECT :
                   ((OnClickGroupItem)args.getCurrentFrag()).onClickGroupItem(groupInfo);
                   groupsDialog.dismiss();
                    break;

                 case REQUEST_DIALOG_FRAG_GROUP_SHOW :
                     /**
                      * just show groups options.
                      */
                     showOptionsDialog(groupInfo);
                    break;
            }

        }

        private void showOptionsDialog(GroupInfo groupInfo) {
            DialogFragment f = GroupsOptionsDialog.newInstance(groupInfo);
            f.show(((GroupOptionsActivity)args.getContext()).getSupportFragmentManager(), "GroupsOptionsDialog");
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int position) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_group, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        setData(holder, position);
        startScrollAnim(position, holder.v);
    }

    private void setData(ViewHolder holder, int position) {
        GroupInfo groupInfo = groupInfoList.get(position);
        holder.tvGroup.setText(groupInfo.getGroupName());
    }

    @Override
    public long getItemId(int arg0) {
        return (long) arg0;
    }

    @Override
    public int getItemCount() {
        return  groupInfoList != null ? groupInfoList.size() : 0;
    }

    protected Context getContext() {
        return groupsDialog == null ? null : groupsDialog.getActivity();
    }

    @Override
    public ArrayList getList() {
        return groupInfoList;
    }

}