package com.idea.todo.adapter.viewpager;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.idea.todo.R;
import com.idea.todo.frag.status.StatusDone;
import com.idea.todo.frag.status.StatusLater;
import com.idea.todo.frag.status.StatusNow;


public  class ViewPagerAdapter extends ViewPagerAdapterBase {
    private Context context;

    public ViewPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;

    }
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0: return StatusNow.getInstance(position);
            case 1: return StatusLater.getInstance(position);
            case 2: return StatusDone.getInstance(position);

            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:return  context.getString(R.string.mainTabNow);
            case 1:return  context.getString(R.string.mainTabLater);
            case 2:return  context.getString(R.string.mainTabDone);
        }
        return null;
    }

}