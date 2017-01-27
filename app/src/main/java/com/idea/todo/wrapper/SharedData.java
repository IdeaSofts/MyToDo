package com.idea.todo.wrapper;

public enum SharedData {
    INSTANCE;
    
    private long mCurrentGroup;

    public long getCurrentGroup() {
        return this.mCurrentGroup;
    }

    public void setCurrentGroup(long group) {
        this.mCurrentGroup = group;
    }
}
