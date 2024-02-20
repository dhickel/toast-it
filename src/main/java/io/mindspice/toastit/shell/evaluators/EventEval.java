package io.mindspice.toastit.shell.evaluators;

import io.mindspice.toastit.App;
import com.github.freva.asciitable.ColumnData;
import io.mindspice.toastit.entries.event.EventEntry;
import io.mindspice.toastit.entries.event.EventManager;
import io.mindspice.mindlib.data.tuples.Pair;
import io.mindspice.toastit.shell.InputPrompt;
import io.mindspice.toastit.shell.ShellCommand;
import io.mindspice.toastit.util.TableConfig;
import io.mindspice.toastit.util.TableUtil;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;


public class EventEval extends ShellEvaluator<EventEval> {
    public final EventManager eventManager = App.instance().getEventManager();

    public EventEval() {
        initBaseCommands();
    }

    public EventEval(List<ShellCommand<EventEval>> userCommands) {
        commands.addAll(userCommands);
        initBaseCommands();
    }

    public void initBaseCommands() {
        commands.addAll(List.of(
                ShellCommand.of("new", EventEval::createNewEvent),
                ShellCommand.of(Set.of("manage", "manager"), EventEval::manageEvents),
                ShellCommand.of("remove past", EventEval::clearPast)
        ));
    }

    @Override
    public String modeDisplay() {
        return TableConfig.EVENT_DASHBOARD_FORMATTER.apply(this) + "\n";
//
    }

    public String pastEventTable() {
        List<ColumnData<EventEntry>> viewColumns = TableConfig.EVENT_OVERVIEW_TABLE;
        return TableUtil.generateTableWithHeader("Past Events", eventManager.getPastEvents(), viewColumns);
    }

    public String futureEventTable() {
        List<ColumnData<EventEntry>> viewColumns = TableConfig.EVENT_OVERVIEW_TABLE;
        return TableUtil.generateTableWithHeader("Future Event", eventManager.getFutureEvents(), viewColumns);
    }

    public String clearPast(String input) {
        if (confirmPrompt("Clear Past Events?")) {
            try {
                eventManager.clearPastEvents();
                clearAndPrint("!!! Cleared Past Events !!!\n");
                return modeDisplay();
            } catch (IOException e) {
                System.err.println(Arrays.toString(e.getStackTrace()));
                clearScreen();
                printLnToTerminal(e.getMessage() + "\n");
                return modeDisplay();
            }
        }
        return modeDisplay();
    }

    public String createNewEvent(String input) {
        EventEntry.Builder eventBuilder = EventEntry.builder();

        List<ColumnData<Pair<String, String>>> columns = TableUtil.createKeyPairColumns("", "");

        Runnable printTable = () ->
                clearAndPrint(TableUtil.generateTableWithHeader("New Event", eventBuilder.toTableState(), columns));

        try {
            printTable.run();
            //Name
            eventBuilder.name = promptInput("Enter Event Name: ");

            //Start Time
            printTable.run();
            LocalDateTime start = promptDateTime("Start");
            if (start == null) { return "Aborted..."; }
            eventBuilder.startTime = start;

            //End time
            printTable.run();
            LocalDateTime end = promptDateTime("End");
            if (end == null) { return "Aborted..."; }
            eventBuilder.endTime = end;

            //Tags
            printTable.run();
            eventBuilder.tags = promptTags("Event tags");

            //Reminders
            printTable.run();
            eventBuilder.reminders = promptReminder(eventBuilder.startTime);

            printTable.run();
            boolean confirmed = false;
            while (!confirmed) {
                confirmed = confirmPrompt("Finished? (No to edit)");
                if (!confirmed) {
                    updateEvent(eventBuilder.build());
                }
            }

            eventManager.addEvent(eventBuilder.build());
            printTable.run();
            promptInput("Created Event, Press Enter To Continue...");
            return modeDisplay();

        } catch (IOException e) {
            System.err.println(Arrays.toString(e.getStackTrace()));
            return e.getMessage();
        }
    }

    public EventEntry updateEvent(EventEntry event) {
        EventEntry.Builder eventBuilder = event.updateBuilder();
        List<ColumnData<Pair<String, String>>> columns = TableUtil.createKeyPairColumns("", "");

        Runnable printTable = () -> {
            try {
                clearAndPrint(TableUtil.generateTableWithHeader("Update Event", eventBuilder.toTableState(), columns) + " \n");
            } catch (Exception e) { System.err.println("Error Printing Event Table"); }
        };

        printTable.run();
        if (confirmPrompt("Replace Name?")) {
            eventBuilder.name = promptInput("Enter New Name: ");
        }

        printTable.run();
        if (confirmPrompt("Replace Start Time?")) {
            eventBuilder.startTime = promptDateTime("New Start");
        }

        printTable.run();
        if (confirmPrompt("Replace End Time?")) {
            eventBuilder.endTime = promptDateTime("New End");
        }

        printTable.run();
        if (confirmPrompt("Replace Tags?")) {
            eventBuilder.tags = promptTags("New Event Tags");
        }

        printTable.run();
        if (confirmPrompt("Replace Reminders?")) {
            eventBuilder.reminders = promptReminder(eventBuilder.startTime);
        }

        printTable.run();
        promptInput("Updated Event, Press Enter To Continue...");
        return eventBuilder.build();

    }



    public String manageEvents(String input) {
        InputPrompt<EventEntry> prompt = new InputPrompt<>(eventManager.getAllEvents());

        String cmds = String.join("\n", "\nAvailable Actions:",
                TableUtil.basicRow(2, "new", "view <index>", "update <index>", "delete <index>", "done"),
                TableUtil.basicRow(2, "filter all", "filter start", "filter <name>", "filter <tag>"),
                TableUtil.basicRow(2, "archive <index>", "archive past"));

        String output = "";

        while (true) {
            try {
                clearAndPrint(TableUtil.generateTable(prompt.getFiltered(), TableConfig.EVENT_MANAGE_TABLE));
                printLnToTerminal(cmds);

                if (!output.isEmpty()) {
                    printLnToTerminal("\n" + output + "\n");
                    output = "";
                }

                String[] userInput = promptInput("Action: ").trim().split(" ");
                switch (userInput[0]) {
                    case String s when s.startsWith("done") -> {
                        clearScreen();
                        return modeDisplay();
                    }
                    case String s when s.startsWith("new") -> {
                        createNewEvent("");
                        prompt = new InputPrompt<>(eventManager.getAllEvents()); // TODO we should handle this differnt?
                    }

                    case String s when s.startsWith("delete") -> output = prompt.create()
                            .validateInputLength(userInput, 2)
                            .validateAndGetIndex(userInput[1])
                            .confirm(this::confirmPrompt, entry -> String.format("Delete Event \"%s\"?", entry.name()))
                            .itemConsumer(eventManager::deleteEvent)
                            .listRemove()
                            .display(entry -> "Deleted:" + entry.name());

                    case String s when s.startsWith("update") -> output = prompt.create()
                            .validateInputLength(userInput, 2)
                            .validateAndGetIndex(userInput[1])
                            .itemUpdate(this::updateEvent)
                            .itemConsumer(eventManager::updateEvent)
                            .display(entry -> "Updated: " + entry.name());

                    case String s when s.startsWith("view") -> output = prompt.create()
                            .validateInputLength(userInput, 2)
                            .validateAndGetIndex(userInput[1])
                            .itemConsumer(item -> {
                                List<ColumnData<Pair<String, String>>> columns = TableUtil.createKeyPairColumns("", "");
                                clearAndPrint(TableUtil.generateTable(item.updateBuilder().toTableState(), columns));
                            })
                            .display(__ -> promptInput("Press Enter To Continue..."));

                    case String s when s.startsWith("filter") -> output = filterPrompt(userInput, prompt);

                    case String s when s.startsWith("archive past") -> output = String.format(
                            "Archived %d Tasks",
                            archiveEntries(t -> t.second().endTime().isBefore(LocalDateTime.now()), prompt, eventManager::archiveEvent)
                    );

                    case String s when s.startsWith("archive") -> output = prompt.create()
                            .validateInputLength(userInput, 2)
                            .validateAndGetIndex(userInput[1])
                            .confirm(this::confirmPrompt, i -> String.format("Archive Event: %s ?", i.name()))
                            .itemConsumer(eventManager::archiveEvent)
                            .listRemove()
                            .display(i -> "Archived: " + i.name());

                    default -> output = "Invalid Input";
                }
            } catch (Exception e) {
                System.err.println(Arrays.toString(e.getStackTrace()));
            }
        }
    }
}


