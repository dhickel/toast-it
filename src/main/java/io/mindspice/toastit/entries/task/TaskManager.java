package io.mindspice.toastit.entries.task;

import io.mindspice.toastit.App;
import io.mindspice.toastit.util.DateTimeUtil;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;


public class TaskManager {

    List<TaskEntry.Stub> activeTasks = new CopyOnWriteArrayList<>();

    public Consumer<TaskManager> refreshActiveTasks = (self) -> {
        try {
            activeTasks.addAll(App.instance().getDatabase().getActiveTasks());

            activeTasks.clear();
            activeTasks.forEach(task -> {
                if (task.dueBy() < DateTimeUtil.localToUnix(LocalDateTime.now())) {

                }
            });


        } catch (IOException e) {
            System.err.println("Failed to refresh active tasks");
        }
    };


}
