package io.mindspice.toastit.shell.evaluators;

import com.github.freva.asciitable.ColumnData;
import io.mindspice.mindlib.data.tuples.Pair;
import io.mindspice.toastit.App;
import io.mindspice.toastit.entries.project.ProjectEntry;
import io.mindspice.toastit.entries.project.ProjectManager;
import io.mindspice.toastit.entries.task.TaskEntry;
import io.mindspice.toastit.shell.InputPrompt;
import io.mindspice.toastit.shell.ShellCommand;
import io.mindspice.toastit.util.Settings;
import io.mindspice.toastit.util.TableConfig;
import io.mindspice.toastit.util.TableUtil;
import io.mindspice.toastit.util.Util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class ProjectEval extends ShellEvaluator<ProjectEval> {
    private final TaskEval taskEval;
    public final ProjectManager projectManager = App.instance().getProjectManager();

    @Override
    public String modeDisplay() {
        clearScreen();
        return TableConfig.PROJECT_DASHBOARD_FORMATTER.apply(this) + "\n";
    }

    public ProjectEval(TaskEval taskEval) {
        this.taskEval = taskEval;
        initBaseCommands();
    }

    public void initBaseCommands() {
        commands.addAll(List.of(
                ShellCommand.of("new", ProjectEval::createNewProject),
                ShellCommand.of("manage", ProjectEval::manageProjects),
                ShellCommand.of("open", ProjectEval::onOpenProject),
                ShellCommand.of("view", ProjectEval::onViewProject)

        ));
    }

    public String activeProjectTable() {
        List<ColumnData<ProjectEntry>> viewColumns = TableConfig.PROJECT_OVERVIEW_TABLE;
        String table = TableUtil.generateTableWithHeader("Active Projects", projectManager.getActiveProjects(), viewColumns);
        String cmds = String.join("\n", "\nAvailable Actions:",
                TableUtil.basicRow(2, "new", "manage", "open <name>", "view <name>"));

        return String.join("\n", table, cmds) + "\n";
    }

    public String projectViewTable(ProjectEntry project) {
        clearScreen();
        String infoTable = TableUtil.generateTableWithHeader("Project: " + project.name(), List.of(Pair.of(-1, project)), TableConfig.PROJECT_MANAGE_TABLE);
        String descTable = TableUtil.generateTable(List.of(project), TableConfig.DESCRIPTION_TABLE);
        String noteTable = TableUtil.generateTable(getIndexedList(project.notes()), TableConfig.NOTE_TABLE);
        String taskTable = TableUtil.generateTableWithHeader("Task", getIndexedList(project.taskObjs()), TableConfig.PROJECT_TASK_TABLE);
        String reminderTable = TableUtil.generateTableWithHeader("Reminders", getIndexedList(project.reminders()), TableConfig.REMINDER_TABLE);
        return String.join("\n\n", infoTable, descTable, noteTable, taskTable, reminderTable);
    }

    public String onViewProject(String s) {
        ProjectEntry project = Util.entryMatch(projectManager.getActiveProjects(), Util.removeFirstWord(s));
        if (project == null) {
            return "Project Not Found";
        } else {
            viewProject(project);
            return modeDisplay();
        }
    }

    public String onOpenProject(String s) {

        ProjectEntry project = Util.entryMatch(projectManager.getActiveProjects(), Util.removeFirstWord(s));
        if (project == null) {
            return "Entry Not Found";
        } else if (project.openWith().isEmpty()) {
            return "No open command specified";
        } else {
            Settings.getEditorOr(project.openWith()).accept(project.projectPath());
            return modeDisplay();
        }
    }

    public String createNewProject(String input) {
        ProjectEntry.Builder projectBuilder = ProjectEntry.builder();

        List<ColumnData<Pair<String, String>>> columns = TableUtil.createKeyPairColumns("", "");

        Runnable printTable = () ->
                clearAndPrint(TableUtil.generateTableWithHeader("New Project", projectBuilder.toTableState(), columns) + "\n");

        try {
            printTable.run();
            // Name
            projectBuilder.name = promptInput("Enter Project Name: ");

            // Description
            printTable.run();
            projectBuilder.description = promptInput("Enter Project Description: ");

            // Project path
            printTable.run();
            if (confirmPrompt("Add Project path?")) {
                projectBuilder.projectPath = promptPath();
                projectBuilder.openWith = promptInput("Editor key or command: ");
            }

            // Tags
            printTable.run();
            projectBuilder.tags = promptTags("Project Tags");

            // tasks
            printTable.run();
            if (confirmPrompt("Add Tasks?")) {
                projectBuilder.taskObjs = promptTasks(projectBuilder.taskObjs);
                projectBuilder.tasks = projectBuilder.taskObjs.stream().map(TaskEntry::uuid).collect(Collectors.toList());
            }

            // Notes
            printTable.run();
            if (confirmPrompt("Add notes?")) {
                projectBuilder.notes = promptNotes();
            }

            // Due By
            printTable.run();
            projectBuilder.dueBy = promptDateTime("Project Due");

            printTable.run();
            if (confirmPrompt("Add Due By Reminders")) {
                projectBuilder.reminders = promptReminder(projectBuilder.dueBy);
            }

            // Prompt to start now
            printTable.run();
            if (confirmPrompt("Start Project Now?")) {
                projectBuilder.started = true;
                projectBuilder.startedAt = LocalDateTime.now();
            } else {
                if (confirmPrompt("Set Start Time")) {
                    projectBuilder.startedAt = promptDateTime("Start Time");
                    projectBuilder.started = confirmPrompt("Set Started?");
                }
            }

            printTable.run();
            ProjectEntry project = projectBuilder.build();
            boolean confirmed = false;
            while (!confirmed) {
                confirmed = confirmPrompt("Finished? (No to edit)");
                if (!confirmed) {
                    project = updateProject(project);
                    clearAndPrint(TableUtil.generateTableWithHeader("New Project", project.updateBuilder().toTableState(), columns) + "\n");
                }
            }

            projectManager.addProject(project);
            promptInput("Project Created, Press Enter To Return...");
            return modeDisplay();

        } catch (IOException e) {
            System.err.println(Arrays.toString(e.getStackTrace()));
            return e.getMessage();
        }
    }

    public ProjectEntry updateProject(ProjectEntry project) {
        ProjectEntry.Builder projectBuilder = project.updateBuilder();

        List<ColumnData<Pair<String, String>>> columns = TableUtil.createKeyPairColumns("", "");

        Runnable printTable = () ->
                clearAndPrint(TableUtil.generateTableWithHeader("Update Project", projectBuilder.toTableState(), columns) + "\n");

        try {
            // Name
            printTable.run();
            if (confirmPrompt("Replace Name?")) {
                projectBuilder.name = promptInput("Enter New Task Name: ");
            }

            // Description
            printTable.run();
            if (confirmPrompt("Replace Description?")) {
                projectBuilder.description = promptInput("Enter New Task Description: ");
            }

            // Project path
            printTable.run();
            if (confirmPrompt("Update Project Path")) {
                projectBuilder.projectPath = promptPath();
            }

            //Editor
            printTable.run();
            if (confirmPrompt("New Editor?")) {
                projectBuilder.openWith = promptInput("New Editor Key or Command: ");
            }

            // Tags
            printTable.run();
            if (confirmPrompt("Update Tags")) {
                projectBuilder.tags = promptTags("Replace Tags");
            }

            // Notes
            printTable.run();
            if (confirmPrompt("Update Notes?")) {
                projectBuilder.notes = updateNotes(projectBuilder.notes);
            }

            // Tasks
            printTable.run();
            if (confirmPrompt("Update Tasks?")) {
                projectBuilder.taskObjs = updateTasks(projectBuilder.taskObjs);
                projectBuilder.tasks = projectBuilder.taskObjs.stream().map(TaskEntry::uuid).toList();
            }

            // Due By
            printTable.run();
            if (confirmPrompt("Replace Due By?")) {
                projectBuilder.dueBy = promptDateTime("Task Due");
                projectBuilder.reminders = new ArrayList<>();
            }

            // Reminders
            printTable.run();
            printLnToTerminal("*If you updated Due By time, existing reminders were wiped");
            if (confirmPrompt("Replace Due By Reminders")) {
                projectBuilder.reminders = promptReminder(projectBuilder.dueBy);
            }

            // Start Time
            printTable.run();
            if (confirmPrompt("Replace Start Time?")) {
                projectBuilder.startedAt = promptDateTime("New Start Time: ");
                projectBuilder.started = confirmPrompt("Set As Started?");
            }

            // Completion Time
            printTable.run();
            if (confirmPrompt("Replace Completed At?")) {
                projectBuilder.completedAt = promptDateTime("New Completed At");
            }

            printTable.run();
            promptInput("Project Updated, Press Enter To Return...");
            return projectBuilder.build();

        } catch (IOException e) {
            System.err.println(Arrays.toString(e.getStackTrace()));
            return project;
        }
    }

    public List<TaskEntry> updateTasks(List<TaskEntry> tasks) throws IOException {
        String cmds = String.join("\n", "\nAvailable Actions:",
                TableUtil.basicRow(2, "new", "edit <index>", "delete <index>", "done")
        );

        InputPrompt<TaskEntry> prompt = new InputPrompt<>(tasks);

        String output = "";
        while (true) {
            String table = TableUtil.generateIndexedPairTable("Tasks", "Task", prompt.getItems(), TaskEntry::name);
            clearAndPrint(table + "\n");

            printLnToTerminal(cmds);
            if (!output.isEmpty()) {
                printLnToTerminal(output + "\n");
                output = "";
            }

            String[] input = promptInput("Action: ").trim().split(" ");
            switch (input[0]) {

                case String s when s.startsWith("new") -> prompt.replaceItems(promptTasks(tasks));

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
                        .itemUpdate(taskEval::updateTask)
                        .display(entry -> "Updated: " + entry.name());

                default -> output = "Invalid input or index";
            }
        }
    }

    public String manageProjects(String input) {
        InputPrompt<ProjectEntry> prompt;
        try {
            prompt = new InputPrompt<>(projectManager.getAllProjects());
        } catch (IOException e) {
            System.err.println(e.getMessage() + " | " + Arrays.toString(e.getStackTrace()));
            return "Error loading Projects: " + e.getMessage();
        }

        String cmds = String.join("\n", "\nAvailable Actions:",
                TableUtil.basicRow(2, "new", "open <index/name>", "view <index/name>", "update <index/name>", "complete <index/name>", "delete <index/name>"),
                TableUtil.basicRow(2, "filter all", "filter completed", "filter start", "filter name <name>", "filter tag <tag>", "filter due"),
                TableUtil.basicRow(2, "archive <index/name>", "archive completed", "done"));

        String output = "";

        while (true) {
            try {
                clearAndPrint(TableUtil.generateTableWithHeader(
                        "Manage Projects",
                        prompt.getFiltered(),
                        TableConfig.PROJECT_MANAGE_TABLE)
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
                        createNewProject("");
                        prompt = new InputPrompt<>(projectManager.getActiveProjects());
                    }

                    case String s when s.startsWith("open") -> {
                        output = Util.isInt(userInput[1])
                                 ? prompt.create()
                                         .validateInputLength(userInput, 2)
                                         .validateAndGetIndex(userInput[1])
                                         .itemConsumer(i -> Settings.getEditorOr(i.openWith()).accept(i.projectPath()))
                                         .display(__ -> "")
                                 : prompt.create()
                                         .validateInputLength(userInput, 2)
                                         .forceSelect(Util.entryMatch(prompt.getItems(), rawInput.replace("open", "")))
                                         .itemConsumer(i -> Settings.getEditorOr(i.openWith()).accept(i.projectPath()))
                                         .display(__ -> "");
                    }

                    case String s when s.startsWith("delete") -> {

                        output = Util.isInt(userInput[1])
                                 ? prompt.create()
                                         .validateInputLength(userInput, 2)
                                         .validateAndGetIndex(userInput[1])
                                         .confirm(this::confirmPrompt, entry -> String.format("Delete Project \"%s\"?", entry.name()))
                                         .itemConsumer(projectManager::deleteProject)
                                         .listRemove()
                                         .display(entry -> "Deleted: " + entry.name())
                                 : prompt.create()
                                         .validateInputLength(userInput, 2)
                                         .forceSelect(Util.entryMatch(prompt.getItems(), rawInput.replace("delete", "")))
                                         .confirm(this::confirmPrompt, item -> String.format("Delete Project: \"%s\"?", item.name()))
                                         .itemConsumer(projectManager::deleteProject)
                                         .listRemove()
                                         .display(item -> "Deleted: " + item.name());
                    }

                    case String s when s.startsWith("update") -> {
                        output = Util.isInt(userInput[1])
                                 ? prompt.create()
                                         .validateInputLength(userInput, 2)
                                         .validateAndGetIndex(userInput[1])
                                         .itemUpdate(this::updateProject)
                                         .itemConsumer(projectManager::updateProject)
                                         .display((entry) -> "Updated: " + entry.name())
                                 : prompt.create()
                                         .validateInputLength(userInput, 2)
                                         .forceSelect(Util.entryMatch(prompt.getItems(), rawInput.replace("update", "")))
                                         .itemUpdate(this::updateProject)
                                         .itemConsumer(projectManager::updateProject)
                                         .display(item -> "Updated: " + item.name());
                    }

                    case String s when s.startsWith("view") -> {
                        output = Util.isInt(userInput[1])
                                 ? prompt.create()
                                         .validateInputLength(userInput, 2)
                                         .validateAndGetIndex(userInput[1])
                                         .itemUpdate(this::viewProject)
                                         .display((__) -> "")
                                 : prompt.create()
                                         .validateInputLength(userInput, 2)
                                         .forceSelect(Util.entryMatch(prompt.getItems(), rawInput.replace("view", "")))
                                         .itemConsumer(this::viewProject)
                                         .display(__ -> "");
                    }

                    case String s when s.startsWith("complete") -> {
                        output = Util.isInt(userInput[1])
                                 ? prompt.create()
                                         .validateInputLength(userInput, 2)
                                         .validateAndGetIndex(userInput[1])
                                         .confirm(this::confirmPrompt, entry -> String.format("Complete Project \"%s\"?", entry.name()))
                                         .itemUpdate(task -> task.asCompleted(LocalDateTime.now()))
                                         .display(entry -> String.format("Set Project: %s as Completed", entry.name()))
                                 : prompt.create()
                                         .validateInputLength(userInput, 2)
                                         .forceSelect(Util.entryMatch(prompt.getItems(), rawInput.replace("view", "")))
                                         .confirm(this::confirmPrompt, entry -> String.format("Complete Project \"%s\"?", entry.name()))
                                         .itemUpdate(task -> task.asCompleted(LocalDateTime.now()))
                                         .display(entry -> String.format("Set Project: %s as Completed", entry.name()));
                    }

                    case String s when s.startsWith("filter") -> output = filterPrompt(userInput, prompt);

                    case String s when s.startsWith("archive completed") -> output = String.format(
                            "Archived %d Projects",
                            archiveEntries(t -> t.second().completed(), prompt, projectManager::archiveProject)
                    );

                    case String s when s.startsWith("archive") -> {
                        output = Util.isInt(userInput[0])
                                 ? prompt.create()
                                         .validateInputLength(userInput, 2)
                                         .validateAndGetIndex(userInput[1])
                                         .confirm(this::confirmPrompt, i -> String.format("Archive Project: %s (Irreversible)", i.name()))
                                         .itemConsumer(projectManager::archiveProject)
                                         .display(i -> "Archived: " + i.name())
                                 : prompt.create()
                                         .validateInputLength(userInput, 2)
                                         .forceSelect(Util.entryMatch(prompt.getItems(), rawInput.replace("archive", "")))
                                         .confirm(this::confirmPrompt, i -> "Archive Project: " + i.name())
                                         .itemConsumer(projectManager::archiveProject)
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

    public ProjectEntry viewProject(ProjectEntry project) {
        String cmds = String.join("\n", "\nAvailable Actions:",
                TableUtil.basicRow(2, "open", "start project", "start task <index>", "start all", "view desc", "view note <index>", "view task <index>"),
                TableUtil.basicRow(2, "update all", "update task <index>", "update desc", "update note <index>"),
                TableUtil.basicRow(2, "start all", "start <index>", "complete all", "complete task <index>", "archive", "done")
        );

        InputPrompt<String> notePrompt = new InputPrompt<>(project.notes());
        InputPrompt<TaskEntry> taskPrompt = new InputPrompt<>(project.taskObjs());
        String description = project.description();

        String output = "";
        while (true) {
            try {
                var updater = project.updateBuilder(); // Simpler to just rebuild each loop to catch updates
                updater.description = description;
                updater.taskObjs = taskPrompt.getItems();
                updater.tasks = taskPrompt.getItems().stream().map(TaskEntry::uuid).toList();
                updater.notes = notePrompt.getItems();

                clearAndPrint(TableConfig.PROJECT_VIEW_FORMATTER.apply(this, project));
                printLnToTerminal(cmds);

                if (!output.isEmpty()) {
                    printLnToTerminal(output + "\n");
                    output = "";
                }

                String[] userInput = promptInput("Action: ").trim().split(" ");

                switch (userInput[0]) {
                    case String s when s.startsWith("done") -> {
                        return project;
                    }

                    case String s when s.startsWith("open") -> Settings.getEditorOr(project.openWith()).accept(project.projectPath());

                    case String s when s.startsWith("start") && userInput.length > 1 -> {
                        switch (userInput[1]) {
                            case String s1 when s1.startsWith("project") -> project = project.asStarted(LocalDateTime.now());

                            case String s1 when s1.startsWith("task") -> output = taskPrompt.create()
                                    .validateInputLength(userInput, 3)
                                    .validateAndGetIndex(userInput[2])
                                    .itemUpdate(item -> item.asStarted(LocalDateTime.now()))
                                    .display(item -> "Started Task: " + item.name());

                            case String s1 when s1.startsWith("all") -> {
                                project = project.asStarted(LocalDateTime.now());
                                taskPrompt.updateAll(item -> item.asStarted(LocalDateTime.now()));
                                output = "Started project and all tasks";
                            }

                            default -> output = "Invalid index or input";
                        }
                    }

                    case String s when s.startsWith("view") && userInput.length > 1 -> {
                        switch (userInput[1]) {
                            case String s1 when s1.startsWith("desc") -> showDataView("Description", project.description());

                            case String s1 when s1.startsWith("note") -> output = notePrompt.create()
                                    .validateInputLength(userInput, 3)
                                    .validateAndGetIndex(userInput[2])
                                    .itemConsumer(item -> showDataView("Note " + userInput[2], item))
                                    .display(__ -> "");

                            case String s1 when s1.startsWith("task") -> output = taskPrompt.create()
                                    .validateInputLength(userInput, 3)
                                    .validateAndGetIndex(userInput[2])
                                    .itemUpdate(taskEval::viewTask)
                                    .display(__ -> "");

                            default -> output = "Invalid index or input";
                        }
                    }

                    case String s when s.startsWith("update") && userInput.length > 1 -> {
                        switch (userInput[1]) {
                            case String s1 when s1.startsWith("all") -> {
                                project = updateProject(project);
                                taskPrompt.replaceItems(project.taskObjs());
                                notePrompt.replaceItems(project.notes());
                                description = project.description();
                                output = "Updated project";
                            }

                            case String s1 when s1.startsWith("task") -> taskPrompt.create()
                                    .validateInputLength(userInput, 3)
                                    .validateAndGetIndex(userInput[2])
                                    .itemUpdate(taskEval::updateTask)
                                    .display(__ -> "Updated Task: " + userInput[2]);

                            case String s1 when s1.startsWith("desc") -> {
                                description = stringEntryUpdater.apply(description);
                                output = "Updated Description";
                            }

                            case String s1 when s1.startsWith("note") -> output = notePrompt.create()
                                    .validateInputLength(userInput, 3)
                                    .validateAndGetIndex(userInput[2])
                                    .itemUpdate(stringEntryUpdater)
                                    .display(__ -> "Updated Note " + userInput[2]);

                            default -> output = "Invalid index or input";
                        }

                    }

                    case String s when s.startsWith("start") && userInput.length > 1 -> {
                        switch (userInput[1]) {
                            case String s1 when s1.startsWith("project") -> {
                                project = project.asStarted(LocalDateTime.now());
                                output = "Started Project";
                            }

                            case String s1 when s1.startsWith("all") -> {
                                taskPrompt.updateAll(t -> t.asStarted(LocalDateTime.now()));
                                output = "Started All SubTasks";
                            }

                            case String s1 when s1.startsWith("task") -> output = taskPrompt.create()
                                    .validateInputLength(userInput, 2)
                                    .validateAndGetIndex(userInput[1])
                                    .itemUpdate(i -> i.asStarted(LocalDateTime.now()))
                                    .display(__ -> "Started Task" + userInput[1]);

                            default -> output = "Invalid index or input";
                        }

                    }

                    case String s when s.startsWith("complete") & userInput.length > 1 -> {
                        switch (userInput[1]) {
                            case String s1 when s1.startsWith("all") -> {
                                taskPrompt.updateAll(i -> i.asCompleted(LocalDateTime.now()));
                                output = "Completed All SubTasks";
                            }

                            case String s1 when s1.startsWith("task") -> output = taskPrompt.create()
                                    .validateInputLength(userInput, 2)
                                    .validateAndGetIndex(userInput[1])
                                    .itemUpdate(i -> i.asCompleted(LocalDateTime.now()))
                                    .display(__ -> "Completed SubTask " + userInput[1]);

                            default -> output = "Invalid index or input";
                        }

                    }

                    case String s when s.startsWith("archive") -> {
                        if (confirmPrompt("Archive Task? (Irreversible)")) {
                            projectManager.archiveProject(project);
                            manageProjects("");
                        }
                    }

                    default -> output = "Invalid input or index";
                }
            } catch (Exception e) {
                e.printStackTrace();
                printLnToTerminal(e.getMessage());
                System.err.println(e.getMessage() + Arrays.toString(e.getStackTrace()));
            }

        }
    }

    public Path promptPath() throws IOException {
        while (true) {
            Path path = Path.of(promptInput("Project Path: "));
            if (!Files.exists(path)) {
                if (!confirmPrompt("Path doesn't exist add anyway?")) {
                    continue;
                }
                boolean create = confirmPrompt("Create Path?");
                if (create) {
                    Files.createDirectories(path);
                }
            }
            return path;
        }
    }

    public List<TaskEntry> promptTasks(List<TaskEntry> tasks) throws IOException {
        do {
            String table = TableUtil.generateKeyPairTable("Tasks", tasks, (__) -> "Task ", TaskEntry::name);
            clearAndPrint(table + "\n");

            tasks.add(taskEval.taskCreator());

            table = TableUtil.generateKeyPairTable("Tasks", tasks, (__) -> "Task ", TaskEntry::name);
            clearAndPrint(table + "\n");
        } while (confirmPrompt("Add another Task?"));
        return tasks;
    }
}

