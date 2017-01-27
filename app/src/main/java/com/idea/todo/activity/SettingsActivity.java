package com.idea.todo.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.idea.todo.R;
import com.idea.todo.activity.file.FileExportActivity;
import com.idea.todo.activity.file.FileImportActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingsActivity extends Activity implements View.OnClickListener {

    @BindView(R.id.btnExport)
    Button btnExport;
    @BindView(R.id.btnImport)
    Button btnImport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        init();
    }

    private void init() {
        btnExport.setOnClickListener(this);
        btnImport.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view == btnExport){
            startActivity(new Intent(this, FileExportActivity.class));
        }
        else if (view == btnImport){
            startActivity(new Intent(this, FileImportActivity.class));
        }
    }
}
