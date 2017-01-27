package com.idea.todo.frag.status;

public class StatusDone extends StatusBase {

    public static StatusDone getInstance(int position) {
        StatusDone instance = new StatusDone();
        instance.setStatus(position);
        return instance;
    }

}
