package util;

import shell.ShellMode;

import java.util.HashMap;
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

    // Events
    public static int EVENT_LOOK_FORWARD_DAYS;
    public static int EVENT_REFRESH_INV_MIN;
    public static int FADE_TIME_SEC;

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




}
