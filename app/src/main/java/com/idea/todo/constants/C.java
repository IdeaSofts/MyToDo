package com.idea.todo.constants;

import com.idea.todo.R;

public interface C {
    int REQUEST_ALERT_DIALOG_TODO_DELETE = 0;
    int REQUEST_ALERT_DIALOG_GROUP_DELETE = 1;
    int REQUEST_ALERT_DIALOG_TODO_DELETE_ALL = 2;

    int REQUEST_DIALOG_FRAG_GROUP_SET = 1;
    int REQUEST_DIALOG_FRAG_GROUP_SELECT = 2;
    int REQUEST_DIALOG_FRAG_GROUP_SHOW = 3;

    String KEY_CURRENT_GROUP = "CURRENT_GROUP";
    String INTENT_KEY_TODO_ID = "ITEM_ID";
    String INTENT_KEY_TODO_DATE = "INTENT_KEY_TODO_DATE";
    String INTENT_KEY_TODO_STATUS = "TODO_STATUS";
    String KEY_SAVED_TEXT = "KEY_SAVED_TEXT";
    String TAG_FRAG_SET_ALARM = "SetAlarmFrag";
    int SORT_CREATED_DATE = 0;
    int SORT_ITEM_DATE = 1;
    int STATUS_DONE = 2;
    int STATUS_LATER = 1;
    int STATUS_NOW = 0;
    int TODO_IS_NEW = -1;
    int GROUP_ID_ALL = 1;
    int GROUP_ID_LATER = 2;
    int GROUP_ID_DONE = 3;

    String AUTO_MOVE = "prefsAutoMove";
    int AUTO_MOVE_DEFAULT = -1;
    String DATE_CURRENT_COLOR = "prefsDateCurrentColor";
    int DATE_CURRENT_COLOR_DEFAULT = -256;
    String PREFS_DATE_FUTURE_COLOR = "prefsDateFutureColor";
    int DATE_FUTURE_COLOR_DEFAULT = -6250336;
    String DATE_PAST_COLOR = "prefsDatePastColor";
    int DATE_PAST_COLOR_DEFAULT = -32768;
    String PREFS_LIST_ITEM_SIZE = "prefsListItemSize";
    int LIST_ITEM_SIZE_DEFAULT = 31;
    String PREFS_LIST_SORT_ORDER = "prefsListSortOrder";
    String EXTENSION_XML = ".xml";
    String FIRST_DIR_NAME = "...";
    boolean LIST_SORT_ORDER_DEFAULT = true;

    int TODO_STATUS_NOW = 0;
    int TODO_STATUS_LATER = 1;
    int TODO_STATUS_DONE = 2;
    int PREFS_ALARM_REQUEST_CODE = 0;
    String ALARM_PREFERENCES = "AlarmClock";
     boolean DEBUG = true;
    String KEY_VOLUME_BEHAVIOR =
            "volume_button_setting";
    String PREF_CLOCK_FACE = "face";

    int[] CLOCKS = {
            R.layout.digital_clock
    };
    String KEY_ALARM_SNOOZE =
            "snooze_duration";
}
