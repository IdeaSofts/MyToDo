package com.idea.todo.activity.file;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.idea.todo.R;
import com.idea.todo.wrapper.file.FileProcessor;
import com.idea.todo.wrapper.file.XmlFileImporter;
import com.idea.todo.db.Database;

import java.io.File;

public class FileImportActivity extends FilePickerActivity implements Callback {
    private Database mDatabase;
    private XmlFileImporter mFileImporter;
    private ProgressBar mImportProgress;
    private Handler mUpdateHandler;

    public FileImportActivity() {
        super(R.layout.activity_file_import);
    }

    @Override
    public Activity currentActivity() {
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        mDatabase = Database.getInstance(this);
        mImportProgress = (ProgressBar) findViewById(R.id.progressBarImport);
        mUpdateHandler = new Handler(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDatabase != null) {
            mDatabase.close();
        }
    }

    public void selectFile(final File selectedFile) {
        String fileName = selectedFile.getName();
        Builder builder = new Builder(this);
        builder.setTitle(R.string.fileImportTitle);
        builder.setMessage(getString(R.string.fileImportConfirmMessage, new Object[]{fileName}));
        builder.setIcon(R.drawable.ic_import);
        builder.setPositiveButton(R.string.ok, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                showOverwriteWarning(selectedFile);
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.create().show();
    }

    private void showOverwriteWarning(File file) {
        final File selectedFile = file;
        Builder builder = new Builder(this);
        builder.setTitle(R.string.warning);
        builder.setMessage(getString(R.string.fileImportWarningMessage));
        builder.setIcon(R.drawable.ic_import);
        builder.setPositiveButton(R.string.ok, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                importFile(selectedFile);
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.create().show();
    }

    private void importFile(final File file) {
        Runnable importRunner = new Runnable() {
            @Override
            public void run() {
                if (mFileImporter != null) {
                    mFileImporter.processFile(file.getPath());
                    mFileImporter = null;
                }
            }
        };
        mFileImporter = new XmlFileImporter(mUpdateHandler, mDatabase);
        recyclerFiles.setEnabled(false);
        new Thread(importRunner, "File Import Thread").start();
    }

    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case FileProcessor.MSG_PROCESS_STARTED :
                mImportProgress.setVisibility(View.VISIBLE);
                mImportProgress.setMax(msg.arg1);
                mImportProgress.setProgress(0);
                break;
            case Database.DB_VERSION :
                mImportProgress.setProgress(mImportProgress.getProgress() + 1);
                break;
            case FileProcessor.MSG_PROCESS_COMPLETED :
                showMessage(true, R.string.fileImportTitle, getString(R.string.fileImportMessageSuccess));
                restoreUI();
                break;
            case FileProcessor.MSG_PROCESS_CANCELLED :
                Toast.makeText(this, R.string.fileImportMessageCancelled, Toast.LENGTH_LONG).show();
                restoreUI();
                break;
            case FileProcessor.MSG_PROCESS_FAILED :
                showMessage(false, R.string.error, getString(R.string.fileImportMessageFailure, (String) msg.obj));
                restoreUI();
                break;
        }
        return false;
    }

    private void restoreUI() {
        recyclerFiles.setEnabled(true);
        mImportProgress.setVisibility(View.GONE);
        updateAdapter();
    }
}
