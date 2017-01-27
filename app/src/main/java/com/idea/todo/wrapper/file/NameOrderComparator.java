package com.idea.todo.wrapper.file;

import java.io.File;
import java.util.Comparator;

/**
 * Created by sha on 23/01/17.
 */

public class NameOrderComparator implements Comparator<File> {
    public int compare(File lhs, File rhs) {
        return lhs.getName().compareTo(rhs.getName());
    }
}
