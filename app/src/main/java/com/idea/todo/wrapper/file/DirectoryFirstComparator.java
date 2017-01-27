package com.idea.todo.wrapper.file;

import java.io.File;
import java.util.Comparator;

/**
 * Created by sha on 23/01/17.
 */

public class DirectoryFirstComparator implements Comparator<File> {
    public int compare(File lhs, File rhs) {
        if (lhs.isDirectory()) {
            if (rhs.isDirectory()) return 0;
            return -1;
        }
        else if (rhs.isDirectory())  return 1;
        else return 0;

    }
}