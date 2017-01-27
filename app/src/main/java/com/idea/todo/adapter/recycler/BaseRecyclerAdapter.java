package com.idea.todo.adapter.recycler;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

import com.idea.todo.activity.BaseActivity;
import com.idea.todo.constants.C;

import java.util.ArrayList;

/**
 * Created by sha on 24/10/16.
 */

public abstract class BaseRecyclerAdapter<VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> implements C {

    protected static final int TYPE_HEADER = 0;
    protected static final int TYPE_ITEM = 1;
    private int lastPosition = 0;

    protected void startScrollAnim(int position, View v) {
        if (position <= lastPosition)
            v.setAnimation(null);
        else {
            TranslateAnimation scrollAnim = new TranslateAnimation(
                    Animation.RELATIVE_TO_SELF,
                    0.0f,
                    Animation.RELATIVE_TO_SELF,
                    0.0f,
                    Animation.RELATIVE_TO_SELF,
                    0.1f,
                    Animation.RELATIVE_TO_SELF,
                    0.0f);
            scrollAnim.setDuration(300);
            v.startAnimation(scrollAnim);
            lastPosition = position;
        }
    }

    protected String getString(int res) {
        return getContext().getString(res);
    }

    protected abstract Context getContext();

    public abstract ArrayList getList();

    public void clearItem(int pos) {
        ArrayList list = getList();
        if (list == null || list.isEmpty()) return;
        list.remove(pos);
        notifyItemRemoved(pos);
    }

    public BaseActivity base(){
        return ((BaseActivity)getContext());
    }
}
