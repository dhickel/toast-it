package io.mindspice.toastit.shell.evaluators;

import com.github.freva.asciitable.ColumnData;
import io.mindspice.mindlib.data.tuples.Pair;
import io.mindspice.toastit.App;
import io.mindspice.toastit.entries.task.SubTask;
import io.mindspice.toastit.entries.task.TaskEntry;
import io.mindspice.toastit.entries.task.TaskManager;
import io.mindspice.toastit.shell.InputPrompt;
import io.mindspice.toastit.shell.ShellCommand;
import io.mindspice.toastit.util.TableConfig;
import io.mindspice.toastit.util.TableUtil;
import io.mindspice.toastit.util.Util;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class TaskEval extends ShellEvaluator<TaskEval> {
    public final TaskManager taskManager = App.instance().getTaskManager();

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
        String subTaskTable = TableUtil.generateTableWithHeader("      SubTasks", getIndexedList(task.subtasks()), TableConfig.TASK_SUBTASK_TABLE);
        String reminderTable = TableUtil.generateTableWithHeader("Reminders", getIndexedList(task.reminders()), TableConfig.REMINDER_TABLE);
        return String.join("\n\n", infoTable, descTable, noteTable, subTaskTable, reminderTable);
    }

    public TaskEval() {
        initBaseCommands();
    }

    public void initBaseCommands() {
        commands.addAll(List.of(
                ShellCommand.of("new", TaskEval::crateNewTask),
                ShellCommand.of("manage", TaskEval::manageTasks)
        ));

    }

    public String crateNewTask(String input) {
        TaskEntry.Builder taskBuilder = TaskEntry.builder();

        List<ColumnData<Pair<String, String>>> columns = TableUtil.createKeyPairColumns("", "");

        Runnable printTable = () -> {
            try {
                clearAndPrint(TableUtil.generateTableWithHeader("New Task", taskBuilder.toTableState(), columns) + "\n");
            } catch (IOException e) { System.err.println("Error Printing Task Table"); }
        };

        try {
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
            boolean confirmed = false;
            while (!confirmed) {
                confirmed = confirmPrompt("Finished? (No to edit)");
                if (!confirmed) {
                    updateTask(taskBuilder.build());
                }
            }

            taskManager.addTask(taskBuilder.build());

            clearAndPrint(TableUtil.generateTableWithHeader("Saved Task", taskBuilder.toTableStateFull(), columns) + "\n");
            promptInput("Task Created, Press Enter To Return");
            return modeDisplay();

        } catch (IOException e) {
            System.err.println(Arrays.toString(e.getStackTrace()));
            return e.getMessage();
        }

    }

    public TaskEntry updateTask(TaskEntry task) {
        TaskEntry.Builder taskBuilder = task.updateBuilder();

        List<ColumnData<Pair<String, String>>> columns = TableUtil.createKeyPairColumns("", "");

        Runnable printTable = () -> {
            try {
                clearAndPrint(TableUtil.generateTableWithHeader("Update Event", taskBuilder.toTableStateFull(), columns) + "\n");
            } catch (IOException e) { System.err.println("Error Printing Task Table"); }
        };

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
            promptInput("Task Updated, Press Enter To Return");
            return taskBuilder.build();

        } catch (IOException e) {
            System.err.println(Arrays.toString(e.getStackTrace()));
            return task;
        }
    }

    public String manageTasks(String input) {
        InputPrompt<TaskEntry> prompt = new InputPrompt<>(getIndexedList(taskManager.getActiveTasks()));

        String cmds = String.format("%nAvailable Actions:%n%s%n%s%n%s",
                TableUtil.basicRow(2, "new", "open <index>", "update <index>", "complete <index>", "delete <index>", "done"),
                TableUtil.basicRow(2, "filter all", "filter completed", "filter start", "filter <name>", "filter <tag>", "filter due"),
                TableUtil.basicRow(2, "archive <index>", "archive completed", "archive date"));

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

                String[] userInput = promptInput("Action: ").trim().split(" ");
                switch (userInput[0]) {
                    case String s when s.startsWith("done") -> {
                        clearScreen();
                        return modeDisplay();
                    }
                    case String s when s.startsWith("new") -> {
                        crateNewTask("");
                        prompt.resetFiltered();
                    }

                    case String s when s.startsWith("delete") -> output = prompt.create()
                            .validateInputLength(userInput, 2)
                            .validateAndGetIndex(userInput[1])
                            .confirm(this::confirmPrompt, entry -> String.format("Delete Task \"%s\"?", entry.name()))
                            .itemConsumer(taskManager::deleteTask)
                            .listRemove()
                            .display(entry -> "Deleted: " + entry.name());

                    case String s when s.startsWith("update") -> output = prompt.create()
                            .validateInputLength(userInput, 2)
                            .validateAndGetIndex(userInput[1])
                            .itemUpdate(this::updateTask)
                            .itemConsumer(taskManager::updateTask)
                            .display((entry) -> "Updated: " + entry.name());

                    case String s when s.startsWith("open") -> output = prompt.create()
                            .validateInputLength(userInput, 2)
                            .validateAndGetIndex(userInput[1])
                            .itemConsumer(this::viewTask)
                            .display((__) -> "");

                    case String s when s.startsWith("complete") -> output = prompt.create()
                            .validateInputLength(userInput, 2)
                            .validateAndGetIndex(userInput[1])
                            .confirm(this::confirmPrompt, entry -> String.format("Complete Task \"%s\"?", entry.name()))
                            .itemUpdate(task -> task.asCompleted(LocalDateTime.now()))
                            .display(entry -> String.format("Set Task: %s as Completed", entry.name()));

                    case String s when s.startsWith("filter") -> output = filterPrompt(userInput, prompt);

                    default -> output = "Invalid input";
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println(Arrays.toString(e.getStackTrace()));
            }
        }
    }

    public String viewTask(TaskEntry task) {
        while (true) {
            try {
                clearAndPrint(TableConfig.TASK_VIEW_FORMATTER.apply(this, task));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return promptInput("await...");
        }

    }

    public String filterPrompt(String[] userInput, InputPrompt<TaskEntry> prompt) {
        if (userInput.length < 2) {
            return "Invalid input or index";
        }

        switch (userInput[1]) {
            case String s1 when s1.startsWith("due") -> {
                LocalDateTime start = promptDate("filter start").atStartOfDay();
                LocalDateTime end = promptDate("filter end").plusDays(1).atStartOfDay();
                return prompt.create()
                        .filter(t -> t.dueBy().isAfter(start) && t.dueBy().isBefore(end))
                        .display(__ -> String.format("Filtered Due By: %s - %s", start, end));
            }

            case String s1 when s1.startsWith("start") -> {
                LocalDateTime start = promptDate("filter start").atStartOfDay();
                LocalDateTime end = promptDate("filter end").plusDays(1).atStartOfDay();
                return prompt.create()
                        .filter(t -> t.startedAt().isAfter(start) && t.startedAt().isBefore(end))
                        .display(__ -> String.format("Filtered Started At: %s - %s", start, end));
            }

            case String s1 when s1.startsWith("completed") -> {
                return prompt.create().filter(TaskEntry::completed).display(__ -> "Filter Completed");
            }

            case String s1 when s1.startsWith("tag") -> {
                if (userInput.length < 3) { // Need to validate here since index is passed to prompt
                    return "Invalid input";
                }
                return prompt.create()
                        .filter(t -> t.tags().contains(userInput[2]))
                        .display(__ -> "Filtered Tag: " + userInput[2]);
            }

            case String s1 when s1.startsWith("name") -> {
                if (userInput.length < 3) {// Need to validate here since index is passed to prompt
                    printLnToTerminal("Invalid input");
                }
                return prompt.create()
                        .filter(t -> t.name().toLowerCase().contains(userInput[2].toLowerCase()))
                        .display(__ -> "Filtered: " + userInput[2]);
            }

            case String s1 when s1.startsWith("all") -> {
                prompt.resetFiltered();
                return "Showing All";
            }
            default -> { return "Invalid input"; }
        }
    }

    public List<String> updateNotes(List<String> notes) throws IOException {

        String output = "";
        while (true) {
            String table = TableUtil.generateIndexedPairTable("Notes", "Note", notes, (note) -> note);
            clearAndPrint(table + "\n");

            String cmds = TableUtil.basicRow(2, "add note", "add reminder", "update <index>", "complete <index>", "delete <index>", "done");
            printLnToTerminal(cmds);

            if (!output.isEmpty()) {
                printLnToTerminal(output + "\n");
                output = "";
            }

            String[] input = promptInput("Action: ").trim().split(" ");
            switch (input[0]) {
                case String s when s.startsWith("new") -> {
                    String note = promptInput("Enter Note (alt-enter for newlines):\\n\"");
                    notes.add(note);
                }
                case String s when s.startsWith("delete") -> {
                    int index;
                    if (input.length < 2 || (index = validateIndexInput(input[1], notes)) == -1) {
                        output = "Invalid input or index";
                        continue;
                    }
                    notes.remove(index);
                }
                case String s when s.startsWith("done") -> {
                    clearScreen();
                    return notes;
                }
                default -> output = "Invalid input or index";
            }
        }
    }

    public List<SubTask> updateSubTasks(List<SubTask> subTasks) throws IOException {

        String output = "";
        while (true) {
            String table = TableUtil.generateIndexedPairTable("SubTasks", "SubTask", subTasks, SubTask::name);
            clearAndPrint(table + "\n");

            String cmds = """
                                \nActions:
                    new
                            delete<index>
                    done
                    """;
            printLnToTerminal(cmds);

            if (!output.isEmpty()) {
                printLnToTerminal(output + "\n");
                output = "";
            }

            String[] input = promptInput("Action: ").trim().split(" ");
            switch (input[0]) {
                case String s when s.startsWith("new") -> promptSubTasks(subTasks);

                case String s when s.startsWith("delete") -> {
                    int index;
                    if (input.length < 2 || (index = validateIndexInput(input[1], subTasks)) == -1) {
                        output = "Invalid input or index";
                        continue;
                    }
                    subTasks.remove(index);
                }
                case String s when s.startsWith("done") -> {
                    clearScreen();
                    return subTasks;
                }
                default -> output = "Invalid input or index";
            }
        }
    }

    public List<String> promptNotes() throws IOException {
        List<String> notes = new ArrayList<>(4);
        do {
            String table = TableUtil.generateKeyPairTable("Notes", notes, (__) -> "Note ", (note) -> note);
            clearAndPrint(table + "\n");
            notes.add(Util.tempEdit(Util.tempNano(terminal), ""));
            //notes.add(promptInput("Enter Note (alt-enter for newlines):\n"));

            table = TableUtil.generateKeyPairTable("Notes", notes, (__) -> "Note ", (note) -> note);
            clearAndPrint(table + "\n");
        } while (confirmPrompt("Add another note?"));
        return notes;
    }

    public List<SubTask> promptSubTasks(List<SubTask> subTasks) throws IOException {
        SubTask.Builder subTask = SubTask.builder();
        do {
            String table = TableUtil.generateKeyPairTable("SubTasks", subTasks, (__) -> "SubTask ", SubTask::name);
            clearAndPrint(table + "\n");
            // Name
            subTask.name = promptInput("SubTask Name: ");

            // Description
            subTask.description = promptInput("SubTask Description: ");

            subTasks.add(subTask.build());
            table = TableUtil.generateKeyPairTable("Notes", subTasks, (__) -> "SubTask ", SubTask::name);
            clearAndPrint(table + "\n");

        } while (confirmPrompt("Add another subTask?"));
        return subTasks;
    }
}
