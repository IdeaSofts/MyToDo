package com.idea.todo.model;

import android.content.Context;
import android.support.v4.app.Fragment;

import com.idea.todo.R;


/**
 * Created by sha on 12/01/17.
 */

public class AlertDialogArgs {
    private Context context;
    private String msg;
    private int
            request,
            btnNegativeRes = R.string.cancel,
            btnPositiveRes = R.string.ok;

    private boolean isPositive;
    private Fragment currentFrag;
    private long
            toDoId,
            toDoGroup;
    private int toDoStatus;
    private boolean isDisplayCancel = true;

    public AlertDialogArgs(
            String msg,
            int request
    )
    {
        this.msg = msg;
        this.request = request;
    }

    public AlertDialogArgs(
            Fragment currentFrag,
            String msg,
            int request
    )
    {
        this.currentFrag = currentFrag;
        this.msg = msg;
        this.request = request;
    }

    public AlertDialogArgs(
            Context context,
            String msg,
            int request
    )
    {
        this.context = context;
        this.msg = msg;
        this.request = request;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getRequest() {
        return request;
    }

    public void setRequest(int request) {
        this.request = request;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public boolean isPositive() {
        return isPositive;
    }

    public void setPositive(boolean positive) {
        isPositive = positive;
    }

    public int getBtnNegativeRes() {
        return btnNegativeRes;
    }

    public void setBtnNegativeRes(int btnNegativeRes) {
        this.btnNegativeRes = btnNegativeRes;
    }

    public int getBtnPositiveRes() {
        return btnPositiveRes;
    }

    public void setBtnPositiveRes(int btnPositiveRes) {
        this.btnPositiveRes = btnPositiveRes;
    }

    public Fragment getCurrentFrag() {
        return currentFrag;
    }

    public void setCurrentFrag(Fragment currentFrag) {
        this.currentFrag = currentFrag;
    }

    public long getToDoId() {
        return toDoId;
    }

    public void setToDoId(long toDoId) {
        this.toDoId = toDoId;
    }

    public long getToDoGroup() {
        return toDoGroup;
    }

    public void setToDoGroup(long toDoGroup) {
        this.toDoGroup = toDoGroup;
    }

    public int getToDoStatus() {
        return toDoStatus;
    }

    public void setToDoStatus(int toDoStatus) {
        this.toDoStatus = toDoStatus;
    }

    public boolean isDisplayBtnCancel() {
        return isDisplayCancel;
    }

    public void setDisplayCancel(boolean displayCancel) {
        isDisplayCancel = displayCancel;
    }
}
