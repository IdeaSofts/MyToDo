package com.idea.todo.activity.file;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import com.idea.todo.R;
import com.idea.todo.activity.BaseActivity;
import com.idea.todo.adapter.recycler.FilePickerAdapter;
import com.idea.todo.wrapper.Utils;
import com.idea.todo.wrapper.file.DirectoryFirstComparator;
import com.idea.todo.wrapper.file.NameOrderComparator;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public abstract class FilePickerActivity extends BaseActivity{
    private static final String KEY_FILE_ROOT = "KEY_FILE_ROOT";

    protected ArrayList<File> files;
    protected FilePickerAdapter mFileListAdapter;
    protected File fileRoot;
    private int mViewLayoutId;

    @BindView(R.id.tvFileInfo)
    protected TextView tvFileInfo;

    @BindView(R.id.recyclerFiles)
    RecyclerView recyclerFiles;

    public FilePickerActivity(int viewLayoutId) {
        mViewLayoutId = viewLayoutId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(mViewLayoutId);
        ButterKnife.bind(this);

        if (!Utils.haveExternalStorage()) {
            finish();
            displayToast(R.string.noExternalStorage);
        }
        restore(savedInstanceState);
        init();
    }

    private void init() {
        recyclerFiles.setLayoutManager(new LinearLayoutManager(this));
        fileRoot = Environment.getExternalStorageDirectory();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_FILE_ROOT, fileRoot.getAbsolutePath());
    }

    private void restore(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            String root = savedInstanceState.getString(KEY_FILE_ROOT);
            if (TextUtils.isEmpty(root)) return;
            File fileRoot = new File(root);
            if (fileRoot.exists()) {
                this.fileRoot = fileRoot;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateAdapter();
    }

    public void updateAdapter() {
        files = new ArrayList<>();
        Collections.addAll(files, fileRoot.listFiles(fileListFilter()));
        Collections.sort(files, new NameOrderComparator());
        Collections.sort(files, new DirectoryFirstComparator());

        Log.e("updateAdapter", "fileRoot.getParent() = " + fileRoot.getParent());
        if (fileRoot.getParent() != null) {
            files.add(0, new File(FIRST_DIR_NAME));
        }
        if (mFileListAdapter == null) {
            mFileListAdapter = new FilePickerAdapter(this, files);
            recyclerFiles.setAdapter(mFileListAdapter);
        } else {
            mFileListAdapter.notifyDataSetChanged();
        }
        updateTvFileInfo();
    }

    private FilenameFilter fileListFilter() {
        return new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                File file = new File(dir, filename);
                if (!file.canRead())  return false;
                else if (file.isDirectory()) return true;
                else if (filename.toLowerCase(Locale.US).endsWith(EXTENSION_XML))  return true;
                return false;
            }
        };
    }

    protected void updateTvFileInfo() {
        tvFileInfo.setText(fileRoot.getAbsolutePath());
    }


    public abstract Activity currentActivity();

    protected void showMessage(boolean success, int title, String message) {
        int iconRes;
        if (success)
            iconRes = R.drawable.ic_done;
        else
            iconRes = R.drawable.ic_delete_all;
        new Builder(this)
        .setTitle(title)
        .setIcon(iconRes)
        .setMessage(message)
        .setPositiveButton(R.string.ok, null)
        .show();
    }

    public void setFileRoot(File fileRoot) {
        this.fileRoot = fileRoot;
    }

    public File getFileRoot() {
        return fileRoot;
    }
}
