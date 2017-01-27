package com.idea.todo.frag.status;

public class StatusLater extends StatusBase {

    public static StatusLater getInstance(int position) {
        StatusLater instance = new StatusLater();
        instance.setStatus(position);
        return instance;
    }
}
