package io.mindspice.toastit.shell.evaluators;

import com.github.freva.asciitable.ColumnData;
import io.mindspice.mindlib.data.tuples.Pair;
import io.mindspice.toastit.App;
import io.mindspice.toastit.entries.project.ProjectEntry;
import io.mindspice.toastit.entries.task.SubTask;
import io.mindspice.toastit.entries.task.TaskEntry;
import io.mindspice.toastit.entries.task.TaskManager;
import io.mindspice.toastit.shell.InputPrompt;
import io.mindspice.toastit.shell.ShellCommand;
import io.mindspice.toastit.util.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;


public class TaskEval extends ShellEvaluator<TaskEval> {
    public final TaskManager taskManager = App.instance().getTaskManager();

    public TaskEval() {
        initBaseCommands();
    }

    public void initBaseCommands() {
        commands.addAll(List.of(
                ShellCommand.of("new", TaskEval::crateNewTask),
                ShellCommand.of("manage", TaskEval::manageTasks),
                ShellCommand.of("view", TaskEval::onViewTask)
        ));

    }

    @Override
    public String modeDisplay() {
        clearScreen();
        return TableConfig.TASK_DASHBOARD_FORMATTER.apply(this) + "\n";
    }

    public String activeTaskTable() {
        List<ColumnData<TaskEntry>> viewColumns = TableConfig.TASK_OVERVIEW_TABLE;
        return TableUtil.generateTableWithHeader("Active Tasks", taskManager.getActiveTasks(), viewColumns);
    }

    public String taskViewTable(TaskEntry task) {
        clearScreen();
        String infoTable = TableUtil.generateTableWithHeader("Task: " + task.name(), List.of(Pair.of(-1, task)), TableConfig.TASK_MANAGE_TABLE);
        String descTable = TableUtil.generateTable(List.of(task), TableConfig.DESCRIPTION_TABLE);
        String noteTable = TableUtil.generateTable(getIndexedList(task.notes()), TableConfig.NOTE_TABLE);
        String subTaskTable = TableUtil.generateTableWithHeader("SubTasks", getIndexedList(task.subtasks()), TableConfig.TASK_SUBTASK_TABLE);
        String reminderTable = TableUtil.generateTableWithHeader("Reminders", getIndexedList(task.reminders()), TableConfig.REMINDER_TABLE);
        return String.join("\n\n", infoTable, descTable, noteTable, subTaskTable, reminderTable);
    }

    public String onViewTask(String s) {
        TaskEntry task = Util.entryMatch(taskManager.getActiveTasks(), Util.removeFirstWord(s));
        if (task == null) {
            return "Project Not Found";
        } else {
            viewTask(task);
            return modeDisplay();
        }
    }

    
    public String crateNewTask(String input) {
        try {
            taskCreator();
        } catch (IOException e) {
            System.err.println(Arrays.toString(e.getStackTrace()));
            return e.getMessage();
        }
        return modeDisplay();
    }

    public TaskEntry taskCreator() throws IOException {
        TaskEntry.Builder taskBuilder = TaskEntry.builder();
        List<ColumnData<Pair<String, String>>> columns = TableUtil.createKeyPairColumns("", "");
        Runnable printTable = () ->
                clearAndPrint(TableUtil.generateTableWithHeader("New Task", taskBuilder.toTableState(), columns) + "\n");

        printTable.run();
        // Name
        taskBuilder.name = promptInput("Enter Task Name: ");

        // Description
        printTable.run();
        taskBuilder.description = promptInput("Enter Task Description: ");

        // Tags
        printTable.run();
        taskBuilder.tags = promptTags("Task Tags");

        // Notes
        printTable.run();
        if (confirmPrompt("Add notes?")) {

            taskBuilder.notes = promptNotes();
        }

        // SubTasks
        printTable.run();
        if (confirmPrompt("Add SubTasks?")) {
            taskBuilder.subtasks = promptSubTasks(taskBuilder.subtasks);
        }

        // Due By
        printTable.run();
        taskBuilder.dueBy = promptDateTime("Task Due");

        printTable.run();
        if (confirmPrompt("Add Due By Reminders")) {
            taskBuilder.reminders = promptReminder(taskBuilder.dueBy);
        }

        // Prompt to start now
        printTable.run();
        if (confirmPrompt("Start Task Now?")) {
            taskBuilder.started = true;
            taskBuilder.startedAt = LocalDateTime.now();
        } else {
            if (confirmPrompt("Set Start Time")) {
                taskBuilder.startedAt = promptDateTime("Start Time");
                taskBuilder.started = confirmPrompt("Set Started?");
            }
        }

        printTable.run();
        TaskEntry task = taskBuilder.build();
        boolean confirmed = false;
        while (!confirmed) {
            confirmed = confirmPrompt("Finished? (No to edit)");
            if (!confirmed) {
                task = updateTask(task);
            }
        }

        taskManager.addTask(task);
        clearAndPrint(TableUtil.generateTableWithHeader("Saved Task", taskBuilder.toTableStateFull(), columns) + "\n");
        promptInput("Task Created, Press Enter To Return...");
        return task;
    }

    public TaskEntry updateTask(TaskEntry task) {
        TaskEntry.Builder taskBuilder = task.updateBuilder();

        List<ColumnData<Pair<String, String>>> columns = TableUtil.createKeyPairColumns("", "");

        Runnable printTable = () ->
                clearAndPrint(TableUtil.generateTableWithHeader("Update Event", taskBuilder.toTableStateFull(), columns) + "\n");

        try {
            // Name
            printTable.run();
            if (confirmPrompt("Replace Name?")) {
                taskBuilder.name = promptInput("Enter New Task Name: ");
            }

            // Description
            printTable.run();
            if (confirmPrompt("Replace Description?")) {
                taskBuilder.description = promptInput("Enter New Task Description: ");
            }

            // Tags
            printTable.run();
            if (confirmPrompt("Update Tags")) {
                taskBuilder.tags = promptTags("Replace Tags");
            }

            // Notes
            printTable.run();
            if (confirmPrompt("Update Notes?")) {
                taskBuilder.notes = updateNotes(taskBuilder.notes);
            }

            // SubTasks
            printTable.run();
            if (confirmPrompt("Update SubTasks?")) {
                taskBuilder.subtasks = updateSubTasks(taskBuilder.subtasks);
            }

            // Due By
            printTable.run();
            if (confirmPrompt("Replace Due By?")) {
                taskBuilder.dueBy = promptDateTime("Task Due");
                taskBuilder.reminders = new ArrayList<>();
            }

            // Reminders
            printTable.run();
            printLnToTerminal("*If you updated Due By time, existing reminders were wiped");
            if (confirmPrompt("Replace Due By Reminders")) {
                taskBuilder.reminders = promptReminder(taskBuilder.dueBy);
            }

            // Start Time
            printTable.run();
            if (confirmPrompt("Replace Start Time?")) {
                taskBuilder.startedAt = promptDateTime("New Start Time: ");
                taskBuilder.started = confirmPrompt("Set As Started?");
            }

            // Completion Time
            printTable.run();
            if (confirmPrompt("Replace Completed At?")) {
                taskBuilder.completedAt = promptDateTime("New Completed At");
            }

            printTable.run();
            promptInput("Task Updated, Press Enter To Return...");
            return taskBuilder.build();

        } catch (IOException e) {
            System.err.println(Arrays.toString(e.getStackTrace()));
            return task;
        }
    }

    public String manageTasks(String input) {
        InputPrompt<TaskEntry> prompt = new InputPrompt<>(taskManager.getActiveTasks());

        String cmds = String.join("\n", "\nAvailable Actions:",
                TableUtil.basicRow(2, "new", "view <index/name>", "update <index/name>", "complete <index/name>", "delete <index/name>"),
                TableUtil.basicRow(2, "filter all", "filter completed", "filter start", "filter name <name>", "filter tag <tag>", "filter due"),
                TableUtil.basicRow(2, "archive <index/name>", "archive completed", "done"));

        String output = "";

        while (true) {
            try {
                clearAndPrint(TableUtil.generateTableWithHeader(
                        "Manage Tasks",
                        prompt.getFiltered(),
                        TableConfig.TASK_MANAGE_TABLE)
                );
                printLnToTerminal(cmds);

                if (!output.isEmpty()) {
                    printLnToTerminal(output + "\n");
                    output = "";
                }

                String rawInput = promptInput("Action: ").trim();
                String[] userInput = rawInput.split(" ");
                switch (userInput[0]) {
                    case String s when s.startsWith("done") -> {
                        clearScreen();
                        return modeDisplay();
                    }
                    case String s when s.startsWith("new") -> {
                        crateNewTask("");
                        prompt = new InputPrompt<>(taskManager.getActiveTasks());
                    }

                    case String s when s.startsWith("delete") -> {
                        output = Util.isInt(userInput[1])
                                 ? prompt.create()
                                         .validateInputLength(userInput, 2)
                                         .validateAndGetIndex(userInput[1])
                                         .confirm(this::confirmPrompt, entry -> String.format("Delete Task \"%s\"?", entry.name()))
                                         .itemConsumer(taskManager::deleteTask)
                                         .listRemove()
                                         .display(entry -> "Deleted: " + entry.name())
                                 : prompt.create()
                                         .validateInputLength(userInput, 2)
                                         .forceSelect(Util.entryMatch(prompt.getItems(), rawInput.replace("delete", "")))
                                         .confirm(this::confirmPrompt, item -> String.format("Delete Project: \"%s\"?", item.name()))
                                         .itemConsumer(taskManager::deleteTask)
                                         .listRemove()
                                         .display(item -> "Deleted: " + item.name());
                    }

                    case String s when s.startsWith("update") -> {
                        output = Util.isInt(userInput[1])
                                 ? prompt.create()
                                         .validateInputLength(userInput, 2)
                                         .validateAndGetIndex(userInput[1])
                                         .itemUpdate(this::updateTask)
                                         .itemConsumer(taskManager::updateTask)
                                         .display((entry) -> "Updated: " + entry.name())
                                 : prompt.create()
                                         .validateInputLength(userInput, 2)
                                         .forceSelect(Util.entryMatch(prompt.getItems(), rawInput.replace("update", "")))
                                         .itemUpdate(this::updateTask)
                                         .display(item -> "Updated: " + item.name());
                    }

                    case String s when s.startsWith("view") -> {
                        output = Util.isInt(userInput[1])
                                 ? prompt.create()
                                         .validateInputLength(userInput, 2)
                                         .validateAndGetIndex(userInput[1])
                                         .itemUpdate(this::viewTask)
                                         .display((__) -> "")
                                 : prompt.create()
                                         .validateInputLength(userInput, 2)
                                         .forceSelect(Util.entryMatch(prompt.getItems(), rawInput.replace("view", "")))
                                         .itemConsumer(this::viewTask)
                                         .display(__ -> "");
                    }

                    case String s when s.startsWith("complete") -> {
                        output = Util.isInt(userInput[1])
                                 ? prompt.create()
                                         .validateInputLength(userInput, 2)
                                         .validateAndGetIndex(userInput[1])
                                         .confirm(this::confirmPrompt, entry -> String.format("Complete Task \"%s\"?", entry.name()))
                                         .itemUpdate(task -> task.asCompleted(LocalDateTime.now()))
                                         .display(entry -> String.format("Set Task: %s as Completed", entry.name()))
                                 : prompt.create()
                                         .validateInputLength(userInput, 2)
                                         .forceSelect(Util.entryMatch(prompt.getItems(), rawInput.replace("view", "")))
                                         .confirm(this::confirmPrompt, entry -> String.format("Complete Task \"%s\"?", entry.name()))
                                         .itemUpdate(task -> task.asCompleted(LocalDateTime.now()))
                                         .display(entry -> String.format("Set Task: %s as Completed", entry.name()));
                    }

                    case String s when s.startsWith("filter") -> output = filterPrompt(userInput, prompt);

                    case String s when s.startsWith("archive completed") -> output = String.format(
                            "Archived %d Tasks",
                            archiveEntries(t -> t.second().completed(), prompt, taskManager::archiveTask)
                    );

                    case String s when s.startsWith("archive") -> {
                        output = Util.isInt(userInput[1])
                                 ? prompt.create()
                                         .validateInputLength(userInput, 2)
                                         .validateAndGetIndex(userInput[1])
                                         .confirm(this::confirmPrompt, i -> "Archive Task" + i.name())
                                         .itemConsumer(taskManager::archiveTask)
                                         .display(i -> "Archived: " + i.name())
                                 : prompt.create()
                                         .validateInputLength(userInput, 2)
                                         .forceSelect(Util.entryMatch(prompt.getItems(), rawInput.replace("archive", "")))
                                         .confirm(this::confirmPrompt, i -> "Archive Task: " + i.name())
                                         .itemConsumer(taskManager::archiveTask)
                                         .listRemove()
                                         .display(i -> "Archived: " + i.name());
                    }

                    default -> output = "Invalid Input";
                }
            } catch (Exception e) {
                System.err.println(Arrays.toString(e.getStackTrace()));
            }
        }
    }

    public TaskEntry viewTask(TaskEntry task) {
        String cmds = String.join("\n", "\nAvailable Actions:",
                TableUtil.basicRow(2, "start task", "view desc", "view note <index>", "view subtask <index>"),
                TableUtil.basicRow(2, "update all", "update subtask <index>", "update desc", "update note <index>"),
                TableUtil.basicRow(2, "start all", "start <index>", "complete all", "complete <index>", "archive", "done")
        );

        InputPrompt<String> notePrompt = new InputPrompt<>(task.notes());
        InputPrompt<SubTask> subTaskPrompt = new InputPrompt<>(task.subtasks());
        String description = task.description();

        String output = "";
        while (true) {
            try {
                clearAndPrint(TableConfig.TASK_VIEW_FORMATTER.apply(this, task));
                printLnToTerminal(cmds);

                if (!output.isEmpty()) {
                    printLnToTerminal(output + "\n");
                    output = "";
                }

                String[] userInput = promptInput("Action: ").trim().split(" ");

                switch (userInput[0]) {
                    case String s when s.startsWith("done") -> {
                        clearScreen();
                        var updated = task.updateBuilder();
                        updated.notes = notePrompt.getItems();
                        updated.subtasks = subTaskPrompt.getItems();
                        updated.description = description;
                        return updated.build();
                    }

                    case String s when s.startsWith("start task") -> task = task.asStarted(LocalDateTime.now());

                    case String s when s.startsWith("view desc") -> showDataView("Description", task.description());

                    case String s when s.startsWith("view note") -> output = notePrompt.create()
                            .validateInputLength(userInput, 3)
                            .validateAndGetIndex(userInput[2])
                            .itemConsumer(item -> showDataView("Note " + userInput[2], item))
                            .display(__ -> "");

                    case String s when s.startsWith("view subtask") -> output = subTaskPrompt.create()
                            .validateInputLength(userInput, 3)
                            .validateAndGetIndex(userInput[2])
                            .itemConsumer(item -> showDataView("SubTask " + userInput[2], item.description()))
                            .display(__ -> "");

                    case String s when s.startsWith("update all") -> {
                        task = updateTask(task);
                        notePrompt.replaceItems(task.notes());
                        subTaskPrompt.replaceItems(task.subtasks());
                        description = task.description();
                        output = "Updated Task";
                    }

                    case String s when s.startsWith("update subtask") -> subTaskPrompt.create()
                            .validateInputLength(userInput, 3)
                            .validateAndGetIndex(userInput[2])
                            .itemUpdate(subtaskUpdater)
                            .display(__ -> "Updated SubTask: " + userInput[2]);

                    case String s when s.startsWith("update desc") -> {
                        description = stringEntryUpdater.apply(description);
                        output = "Updated Description";
                    }

                    case String s when s.startsWith("update note") -> output = notePrompt.create()
                            .validateInputLength(userInput, 3)
                            .validateAndGetIndex(userInput[2])
                            .itemUpdate(stringEntryUpdater)
                            .display(__ -> "Updated Note " + userInput[2]);

                    case String s when s.startsWith("start all") -> {
                        subTaskPrompt.updateAll(t -> t.asStarted(LocalDateTime.now()));
                        output = "Started All SubTasks";
                    }

                    case String s when s.startsWith("start") -> output = subTaskPrompt.create()
                            .validateInputLength(userInput, 2)
                            .validateAndGetIndex(userInput[1])
                            .itemUpdate(i -> i.asStarted(LocalDateTime.now()))
                            .display(__ -> "Started SubTask" + userInput[1]);

                    case String s when s.startsWith("complete all") -> {
                        subTaskPrompt.updateAll(i -> i.asCompleted(LocalDateTime.now()));
                        output = "Completed All SubTasks";
                    }

                    case String s when s.startsWith("complete") -> output = subTaskPrompt.create()
                            .validateInputLength(userInput, 2)
                            .validateAndGetIndex(userInput[1])
                            .itemUpdate(i -> i.asCompleted(LocalDateTime.now()))
                            .display(__ -> "Completed SubTask " + userInput[1]);

                    case String s when s.startsWith("archive") -> {
                        if (confirmPrompt("Archive Task?")) {
                            var updated = task.updateBuilder();
                            updated.notes = notePrompt.getItems();
                            updated.subtasks = subTaskPrompt.getItems();
                            updated.description = description;
                            TaskEntry updatedTask = updated.build();
                            taskManager.archiveTask(updatedTask);
                            manageTasks("");
                        }
                    }

                    default -> output = "Invalid input or index";

                }
            } catch (IOException e) {
                printLnToTerminal(e.getMessage());
                System.err.println(e.getMessage() + Arrays.toString(e.getStackTrace()));
            }

        }
    }

    public List<SubTask> updateSubTasks(List<SubTask> subTasks) throws IOException {
        String cmds = String.join("\n", "\nAvailable Actions:",
                TableUtil.basicRow(2, "new", "edit <index>", "delete <index>", "done")
        );

        InputPrompt<SubTask> prompt = new InputPrompt<>(subTasks);

        String output = "";
        while (true) {
            String table = TableUtil.generateIndexedPairTable("SubTasks", "SubTask", prompt.getItems(), SubTask::name);
            clearAndPrint(table + "\n");

            printLnToTerminal(cmds);
            if (!output.isEmpty()) {
                printLnToTerminal(output + "\n");
                output = "";
            }

            String[] input = promptInput("Action: ").trim().split(" ");
            switch (input[0]) {

                case String s when s.startsWith("new") -> prompt.replaceItems(promptSubTasks(subTasks));

                case String s when s.startsWith("done") -> {
                    clearScreen();
                    return prompt.getItems();
                }

                case String s when s.startsWith("delete") -> output = prompt.create()
                        .validateInputLength(input, 2)
                        .validateAndGetIndex(input[1])
                        .confirm(this::confirmPrompt, entry -> String.format("Delete SubTask: \"%s\"?", entry.name()))
                        .listRemove()
                        .display(entry -> "Deleted: " + entry.name());

                case String s when s.startsWith("edit") -> output = prompt.create()
                        .validateInputLength(input, 2)
                        .validateAndGetIndex(input[1])
                        .itemUpdate(subtaskUpdater)
                        .display(entry -> "Updated: " + entry.name());

                default -> output = "Invalid input or index";
            }
        }
    }

    public List<SubTask> promptSubTasks(List<SubTask> subTasks) throws IOException {
        do {
            String table = TableUtil.generateKeyPairTable("SubTasks", subTasks, (__) -> "SubTask ", SubTask::name);
            clearAndPrint(table + "\n");

            subTasks.add(subtaskCreator.get());

            table = TableUtil.generateKeyPairTable("SubTasks", subTasks, (__) -> "SubTask ", SubTask::name);
            clearAndPrint(table + "\n");
        } while (confirmPrompt("Add another subTask?"));
        return subTasks;
    }

    public UnaryOperator<SubTask> subtaskUpdater = (subtask) -> {
        SubTask.Builder updater = subtask.updateBuilder();

        if (confirmPrompt("Update Name?")) {
            updater.name = promptInput("Enter New Name: ");
        }
        if (confirmPrompt("Update description?")) {
            updater.description = stringEntryUpdater.apply(updater.description);
        }
        if (confirmPrompt("Update Completion?")) {
            boolean completed = confirmPrompt("Is Completed?");
            updater.completed = completed;
            updater.completedAt = completed ? LocalDateTime.now() : DateTimeUtil.MAX;
        }
        return updater.build();
    };

    public Supplier<SubTask> subtaskCreator = () -> {
        SubTask.Builder subTask = SubTask.builder();
        subTask.name = promptInput("SubTask Name: ");
        // Description
        subTask.description = promptInput("SubTask Description: ");
        return subTask.build();
    };


}
