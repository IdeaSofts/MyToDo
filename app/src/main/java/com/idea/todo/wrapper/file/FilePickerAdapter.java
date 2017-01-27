package com.idea.todo.wrapper.file;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.idea.todo.R;

import java.io.File;
import java.util.ArrayList;

public class FilePickerAdapter extends ArrayAdapter<File> {
    private Context mContext;
    private ArrayList<File> mFiles;

    private static class FileHolder {
        ImageView fileIcon;
        TextView fileName;

        private FileHolder() {
        }
    }

    public FilePickerAdapter(Context context, int layoutId, ArrayList<File> files) {
        super(context, layoutId, files);
        this.mContext = context;
        this.mFiles = files;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        FileHolder holder;
        View layoutView = convertView;
        if (layoutView == null) {
            layoutView = ((Activity) this.mContext).getLayoutInflater().inflate(R.layout.list_item_file_picker, parent, false);
            holder = new FileHolder();
            holder.fileIcon = (ImageView) layoutView.findViewById(R.id.ivFileIcon);
            holder.fileName = (TextView) layoutView.findViewById(R.id.tvFileName);
            layoutView.setTag(holder);
        }
        else {
            holder = (FileHolder) layoutView.getTag();
        }
        File file = this.mFiles.get(position);
        String fileName = file.getName();
        if (file.isDirectory()) {
            holder.fileIcon.setImageResource(R.drawable.folder);
            holder.fileName.setText(fileName);
        } else {
            holder.fileIcon.setImageResource(R.drawable.file);
            holder.fileName.setText(fileName);
        }
        return layoutView;
    }
}
