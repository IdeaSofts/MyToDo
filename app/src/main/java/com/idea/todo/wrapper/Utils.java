package com.idea.todo.wrapper;

import android.os.Environment;

import java.util.Calendar;
import java.util.Locale;

public class Utils {

    public static boolean haveExternalStorage() {
        return "mounted".equals(Environment.getExternalStorageState());
    }

    public static String getExportFilename() {
        Calendar c = Calendar.getInstance();
        return String.format(Locale.US, "MyToDo_%04d%02d%02d%02d%02d",
                c.get(Calendar.YEAR) + 1900, c.get(Calendar.MONTH) + 1,
                c.getTime().getTime(),
                c.get(Calendar.HOUR),
                c.get(Calendar.MINUTE));
    }
}
