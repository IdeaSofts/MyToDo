package com.idea.todo.wrapper.file;

import android.os.Handler;
import android.os.Message;

import com.idea.todo.constants.C;
import com.idea.todo.db.Database;

public abstract class FileProcessor implements C{
    public static final int MSG_PROCESS_CANCELLED = 3;
    public static final int MSG_PROCESS_COMPLETED = 2;
    public static final int MSG_PROCESS_FAILED = 4;
    public static final int MSG_PROCESS_STARTED = 0;
    public static final int MSG_PROCESS_UPDATED = 1;
    private boolean mCancelProcess;
    protected Database mDatabase;
    private Handler mUpdateHandler;

    FileProcessor(Handler updateHandler, Database database) {
        mDatabase = database;
        mUpdateHandler = updateHandler;
    }

    public abstract void processFile(String filePath);

    public void cancel() {
        mCancelProcess = true;
    }

    void sendMessageStarted(int total) {
        if (mUpdateHandler != null) {
            Message msg = new Message();
            msg.what = MSG_PROCESS_STARTED;
            msg.arg1 = total;
            mUpdateHandler.sendMessage(msg);
        }
    }

    void sendMessageUpdated() {
        if (mUpdateHandler != null) {
            mUpdateHandler.sendEmptyMessage(MSG_PROCESS_UPDATED);
        }
    }

    void sendMessageCompleted() {
        if (mUpdateHandler != null) {
            mUpdateHandler.sendEmptyMessage(MSG_PROCESS_COMPLETED);
        }
    }

    protected void sendMessageCancelled() {
        if (mUpdateHandler != null) {
            mUpdateHandler.sendEmptyMessage(MSG_PROCESS_CANCELLED);
        }
    }

    void sendMessageFailed(String text) {
        if (mUpdateHandler != null) {
            Message msg = new Message();
            msg.what = MSG_PROCESS_FAILED;
            msg.obj = text;
            mUpdateHandler.sendMessage(msg);
        }
    }
}
