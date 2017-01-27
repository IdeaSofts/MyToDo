package com.idea.todo.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.idea.todo.frag.PrefsFrag;

/**
 * Created by sha on 24/01/17.
 */

public class PrefsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new PrefsFrag(), "PrefsFrag")
                .commit();
    }
}
