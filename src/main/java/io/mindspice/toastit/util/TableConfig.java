package io.mindspice.toastit.util;

import com.github.freva.asciitable.ColumnData;
import io.mindspice.mindlib.data.tuples.Pair;
import io.mindspice.toastit.entries.event.EventEntry;
import io.mindspice.toastit.entries.task.SubTask;
import io.mindspice.toastit.entries.task.TaskEntry;
import io.mindspice.toastit.notification.Reminder;
import io.mindspice.toastit.shell.evaluators.EventEval;
import io.mindspice.toastit.shell.evaluators.TaskEval;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;


public class TableConfig {
    public static Character[] BORDER;

    // Event
    public static List<ColumnData<Pair<Integer, EventEntry>>> EVENT_MANAGE_TABLE;
    public static List<ColumnData<EventEntry>> EVENT_OVERVIEW_TABLE;
    public static Function<EventEval, String> EVENT_DASHBOARD_FORMATTER;


    //Task
    public static List<ColumnData<Pair<Integer, TaskEntry>>> TASK_MANAGE_TABLE;
    public static List<ColumnData<TaskEntry>> TASK_OVERVIEW_TABLE;
    public static List<ColumnData<Pair<Integer, SubTask>>> TASK_SUBTASK_TABLE;
    public static Function<TaskEval, String> TASK_DASHBOARD_FORMATTER;
    public static BiFunction<TaskEval, TaskEntry, String> TASK_VIEW_FORMATTER;

    // General
    public static List<ColumnData<TaskEntry>> DESCRIPTION_TABLE;
    public static List<ColumnData<Pair<Integer ,String>>> NOTE_TABLE;
    public static List<ColumnData<Pair<Integer, Reminder>>> REMINDER_TABLE;

}
