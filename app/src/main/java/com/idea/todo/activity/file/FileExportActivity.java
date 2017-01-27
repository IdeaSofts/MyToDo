package com.idea.todo.activity.file;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.idea.todo.R;
import com.idea.todo.db.Database;
import com.idea.todo.wrapper.Utils;
import com.idea.todo.wrapper.file.FileProcessor;
import com.idea.todo.wrapper.file.XmlFileExporter;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FileExportActivity extends FilePickerActivity implements Callback, OnClickListener {
    private Database mDatabase;

    @BindView(R.id.btnExport)
    Button btnExport;
    @BindView(R.id.inputFileExportName)
    EditText inputFileExportName;
    @BindView(R.id.progressBarExport)
    ProgressBar progressBarExport;

    private XmlFileExporter xmlFileExporter;
    private Handler mUpdateHandler;
    
    public FileExportActivity() {
        super(R.layout.activity_file_export);
    }

    @Override
    public Activity currentActivity() {
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        init();
    }

    private void init() {
        mDatabase = Database.getInstance(this);
        mUpdateHandler = new Handler(this);
        inputFileExportName.setText(Utils.getExportFilename());
        inputFileExportName.selectAll();
        inputFileExportName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable) {
                updateTvFileInfo();
            }
        });
        btnExport.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view == btnExport){
            exportFile();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDatabase != null) {
            mDatabase.close();
        }
    }

    @Override
    protected void updateTvFileInfo() {
        String filename = getExportFileName();
        if (filename.length() == 0) {
            tvFileInfo.setText(fileRoot.getAbsolutePath());
            btnExport.setEnabled(false);
            return;
        }
        String filepath = getExportFilePath(filename);
        tvFileInfo.setText(filepath);
        File file = new File(filepath);

        if (!file.exists()) {
            btnExport.setEnabled(true);
        }
    }

    private String getExportFileName() {
        return getInputTxt(inputFileExportName);
    }

    private String getExportFilePath(String filename) {
        if (fileRoot.getParent() == null) {
            return String.format("/%s%s", filename, EXTENSION_XML);
        }
        return String.format("%s/%s%s", fileRoot.getAbsolutePath(), filename, EXTENSION_XML);
    }

    private void exportFile() {
        Runnable exportRunner = new Runnable() {
            @Override
            public void run() {
                if (xmlFileExporter != null) {
                    xmlFileExporter.processFile(getExportFilePath(getExportFileName()));
                    xmlFileExporter = null;
                }
            }
        };
        xmlFileExporter = new XmlFileExporter(mUpdateHandler, mDatabase);
        recyclerFiles.setEnabled(false);
        inputFileExportName.setVisibility(View.GONE);
        btnExport.setEnabled(false);
        new Thread(exportRunner, "File Export Thread").start();
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case FileProcessor.MSG_PROCESS_STARTED :
                progressBarExport.setVisibility(View.VISIBLE);
                progressBarExport.setMax(msg.arg1);
                progressBarExport.setProgress(0);
                return true;
            case FileProcessor.MSG_PROCESS_UPDATED :
                progressBarExport.setProgress(progressBarExport.getProgress() + 1);
                return true;
            case FileProcessor.MSG_PROCESS_COMPLETED :
                showMessage(
                        true,
                        R.string.fileExportTitle,
                        getString(R.string.fileExportMessageSuccess));
                restoreUI();
                return true;
            case FileProcessor.MSG_PROCESS_CANCELLED :
                displayToast(R.string.fileExportMessageCancelled);
                restoreUI();
                return true;
            case FileProcessor.MSG_PROCESS_FAILED :
                showMessage(
                        false,
                        R.string.error,
                        getString(R.string.fileExportMessageFailure, (String) msg.obj));
                restoreUI();
                return true;
            default:
                return false;
        }
    }

    private void restoreUI() {
        recyclerFiles.setEnabled(true);
        inputFileExportName.setVisibility(View.VISIBLE);
        progressBarExport.setVisibility(View.GONE);
        updateAdapter();
    }
}
