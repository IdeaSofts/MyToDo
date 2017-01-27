package com.idea.todo.frag;

import android.support.v4.app.Fragment;

import com.idea.todo.activity.MainActivity;
import com.idea.todo.constants.C;

public class BaseFrag extends Fragment implements C{


    protected MainActivity base(){
        return (MainActivity) getActivity();
    }
}
