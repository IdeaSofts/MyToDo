package com.idea.todo.adapter.recycler;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.idea.todo.R;
import com.idea.todo.activity.file.FileImportActivity;
import com.idea.todo.activity.file.FilePickerActivity;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FilePickerAdapter extends BaseRecyclerAdapter<FilePickerAdapter.ViewHolder>{
    private FilePickerActivity filePickerActivity;
    private ArrayList<File> files;
    private View selectedItem;

    public FilePickerAdapter(FilePickerActivity filePickerActivity, ArrayList<File> files) {
        this.files = files;
        this.filePickerActivity = filePickerActivity;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        View v;
        @BindView(R.id.tvFileName)
        TextView tvFileName;
        @BindView(R.id.ivFileIcon)
        ImageView ivFileIcon;
        ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
            this.v = v;
            this.v.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (view == v){
                setItemSelected();

                File fileRoot = Environment.getExternalStorageDirectory();
                File file = files.get((getLayoutPosition()));
                if (file.getName().equals(FIRST_DIR_NAME)) {
                    File parentDir = fileRoot.getParentFile();
                    if (parentDir != null) {
                        if (parentDir.equals(filePickerActivity.getFileRoot())) return;
                        filePickerActivity.setFileRoot(fileRoot);
                        filePickerActivity.updateAdapter();
                    }
                }
                else if (file.isDirectory()) {
                    fileRoot = new File(fileRoot, file.getName());
                    filePickerActivity.setFileRoot(fileRoot);
                    filePickerActivity.updateAdapter();
                }
                else if (file.isFile()) {

                    Activity currentActivity = filePickerActivity.currentActivity();
                    if (currentActivity instanceof FileImportActivity)
                        ((FileImportActivity)currentActivity).selectFile(file);

                }
            }
        }

        private void setItemSelected() {
            /**
             * If null there is no any selected view
             * select the item and store the view
             */
            if (selectedItem == null){
                v.setSelected(true);
                selectedItem = v;
                selectedItem.setTag(getLayoutPosition());
                return;
            }
            /**
             * if the position equals the selected position do no thing
             */
            if ((int) selectedItem.getTag() == getLayoutPosition()) return;
            /**
             * select the item and update the old one.
             */
            selectedItem = v;
            v.setSelected(true);
            selectedItem.setTag(getLayoutPosition());
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int position) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_file_picker, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        updateSelectedItem(holder, position);
        setData(holder, position);
        startScrollAnim(position, holder.v);
    }

    private void updateSelectedItem(ViewHolder holder, int position) {
        if (selectedItem == null) return;
        if (position == (int) selectedItem.getTag())
            holder.v.setSelected(true);
        else
            holder.v.setSelected(false);
    }

    private void setData(ViewHolder holder, int position) {
        File file = this.files.get(position);
        String fileName = file.getName();
        int iconRes;
        if (file.isDirectory() || fileName.equals(FIRST_DIR_NAME))
            iconRes = R.drawable.ic_folder;
        else
            iconRes = R.drawable.ic_file;

        holder.ivFileIcon.setImageResource(iconRes);
        holder.tvFileName.setText(fileName);
    }

    @Override
    public long getItemId(int arg0) {
        return (long) arg0;
    }

    @Override
    public int getItemCount() {
        return  files != null ? files.size() : 0;
    }

    protected Context getContext() {
        return filePickerActivity;
    }

    @Override
    public ArrayList getList() {
        return files;
    }

}