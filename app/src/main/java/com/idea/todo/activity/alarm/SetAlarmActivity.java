package com.idea.todo.activity.alarm;

import android.os.Bundle;

import com.idea.todo.activity.BaseActivity;
import com.idea.todo.frag.alarm.SetAlarmFrag;

/**
 * Created by sha on 24/01/17.
 */

public class SetAlarmActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SetAlarmFrag(), TAG_FRAG_SET_ALARM)
                .commit();
    }

    @Override
    public void onBackPressed() {
        ((SetAlarmFrag)getFragmentManager().findFragmentByTag(TAG_FRAG_SET_ALARM)).onBackPressed();
    }
}
