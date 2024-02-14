package io.mindspice.toastit.shell.evaluators;

import com.github.freva.asciitable.ColumnData;
import io.mindspice.mindlib.data.tuples.Pair;
import io.mindspice.toastit.App;
import io.mindspice.toastit.entries.task.SubTask;
import io.mindspice.toastit.entries.task.TaskEntry;
import io.mindspice.toastit.entries.task.TaskManager;
import io.mindspice.toastit.shell.ShellCommand;
import io.mindspice.toastit.util.TableUtil;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;


public class TaskEval extends ShellEvaluator<TaskEval> {
    public final TaskManager taskManager = App.instance().getTaskManager();

    @Override
    protected String modeDisplay() {
        return "Task Mode Entered";
    }

    public TaskEval() {
        initBaseCommands();
    }

    public void initBaseCommands() {
        commands.addAll(List.of(
                ShellCommand.of("new", TaskEval::crateNewTask)
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

            taskManager.addTask(TaskEntry.builder().build());

            clearAndPrint(TableUtil.generateTableWithHeader("Saved Task", taskBuilder.toTableStateFull(), columns) + "\n");
            promptInput("Task Created, Press Enter To Return");
            return modeDisplay.get();

        } catch (IOException e) {
            System.err.println(Arrays.toString(e.getStackTrace()));
            return e.getMessage();
        }

    }

    public String updateTask(TaskEntry task) {
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

            printTable.run();
            if (confirmPrompt("Replace Start Time?")) {
                taskBuilder.startedAt = promptDateTime("New Start Time: ");
                taskBuilder.started = confirmPrompt("Set As Started?");
            }

            printTable.run();
            if (confirmPrompt("Replace Completed At?")) {
                taskBuilder.completedAt = promptDateTime("New Completed At");
            }

            taskManager.updateTask(taskBuilder.build());
            printTable.run();
            promptInput("Task Updated, Press Enter To Return");
            return modeDisplay.get();
        } catch (IOException e) {
            System.err.println(Arrays.toString(e.getStackTrace()));
            return e.getMessage();
        }
    }

    public List<String> updateNotes(List<String> notes) throws IOException {

        String output = "";
        while (true) {
            String table = TableUtil.generateIndexedPairTable("Notes", "Note", notes, (note) -> note);
            clearAndPrint(table + "\n");

            String cmds = """
                    \nActions:
                        new
                        delete <index>
                        exit
                    """;
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
                case String s when s.startsWith("exit") -> {
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
                        delete <index>
                        exit
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
                case String s when s.startsWith("exit") -> {
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

            notes.add(promptInput("Enter Note (alt-enter for newlines):\n"));

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
