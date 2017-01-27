package com.idea.todo.model;

import android.content.Context;
import android.support.v4.app.Fragment;

/**
 * Created by sha on 12/01/17.
 */

public class GroupsDialogArgs {
    private int request;
    private Fragment currentFrag;
    private Context context;

    public GroupsDialogArgs(int request){
        this.request = request;
    }

    public GroupsDialogArgs(Fragment currentFrag, int request) {
        this.currentFrag = currentFrag;
        this.request = request;
    }

    public int getRequest() {
        return request;
    }

    public Fragment getCurrentFrag() {
        return currentFrag;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
