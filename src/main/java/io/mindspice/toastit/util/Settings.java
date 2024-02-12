package io.mindspice.toastit.util;

import com.github.freva.asciitable.HorizontalAlign;
import io.mindspice.toastit.enums.NotificationLevel;
import io.mindspice.toastit.shell.ShellMode;

import java.util.List;
import java.util.Map;


public class Settings {
    // Paths
    public static String ROOT_PATH;
    public static String DATABASE_PATH;
    public static String TASK_PATH;
    public static String NOTE_PATH;
    public static String JOURNAL_PATH;
    public static String PROJECT_PATH;

    // TAGS
    public static Map<String, Tag> TAG_MAP = Map.of();
    public static Tag DEFAULT_TAG = Tag.Default();

    public static Tag getTag(String tag){
        return TAG_MAP.getOrDefault(tag, DEFAULT_TAG);
    }

    // Application
    public static int EXEC_THREADS;
    public static List<String> DATE_INPUT_PATTERNS;
    public static List<String> TIME_INPUT_PATTERNS;
    public static String DATE_TIME_FULL_PATTERN;
    public static String DATE_TIME_SHORT_PATTERN;

    // Events
    public static int EVENT_LOOK_FORWARD_DAYS;
    public static int EVENT_REFRESH_INV_MIN;
    public static int EVENT_NOTIFY_FADE_TIME_SEC;

    // TASKS
    public static int TASK_REFRESH_INV_MIN;
    public static NotificationLevel TASK_OVER_DUE_NOTIFY_LEVEL;
    public static int TASK_NOTIFY_FADE_TIME_SEC;

    // Shell Config
    public static String SHELL_BIND_ADDRESS;
    public static int SHELL_BIND_PORT;
    public static String SHELL_USER;
    public static String SHELL_PASSWORD;
    public static String SHELL_KEY_PAIR;

    public static List<ShellMode<?>> SHELL_MODES;

    // Calendar
    public static int CALENDAR_HEADER_LEADING_SPACES;
    public static int CALENDER_HEADER_HEIGHT;
    public static int CALENDER_CELL_HEIGHT;
    public static int CALENDER_CELL_WIDTH;

    // Global Table Settings
    public static int TABLE_MAX_COLUMN_WIDTH;
    public static int TABLE_HORIZONTAL_LIST_SPACING = 10;
    public static HorizontalAlign TABLE_DEFAULT_ALIGNMENT;




}
