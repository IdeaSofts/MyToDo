package com.idea.todo.frag.status;

import android.os.Bundle;

import com.idea.todo.wrapper.DateWrapper;

public class StatusNow extends StatusBase {

    public static StatusNow getInstance(int position) {
        StatusNow instance = new StatusNow();
        instance.setStatus(position);
        return instance;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            for (Long i : mDatabase.getMoveableItems()) {
                long diff = DateWrapper.daysToGo(mDatabase.getToDo(i).getDate());
                int autoMove = mSharedPreferences.getInt(AUTO_MOVE, -1);
                if (autoMove != -1 && diff <= ((long) autoMove)) {
                    mDatabase.setItemMoved(i);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
