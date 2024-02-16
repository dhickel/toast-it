package io.mindspice.toastit.shell.evaluators;

import io.mindspice.toastit.App;
import com.github.freva.asciitable.ColumnData;
import io.mindspice.toastit.entries.event.EventEntry;
import io.mindspice.toastit.entries.event.EventManager;
import io.mindspice.mindlib.data.tuples.Pair;
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

        Runnable printTable = () -> {
            try {
                clearAndPrint(TableUtil.generateTableWithHeader("New Event", eventBuilder.toTableState(), columns));
            } catch (IOException e) { System.err.println("Error Printing Event Table"); }
        };

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
            promptInput("Created Event, Press Enter To Continue");
            return modeDisplay();

        } catch (IOException e) {
            System.err.println(Arrays.toString(e.getStackTrace()));
            return e.getMessage();
        }
    }

    public String updateEvent(EventEntry event) {
        EventEntry.Builder eventBuilder = event.updateBuilder();
        List<ColumnData<Pair<String, String>>> columns = TableUtil.createKeyPairColumns("", "");

        Runnable printTable = () -> {
            try {
                clearAndPrint(TableUtil.generateTableWithHeader("Update Event", eventBuilder.toTableState(), columns) + " \n");
            } catch (Exception e) { System.err.println("Error Printing Event Table"); }
        };

        try {
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

            eventManager.updateEvent(eventBuilder.build());
            printTable.run();
            promptInput("Updated Event, Press Enter To Continue");
            return modeDisplay();

        } catch (IOException e) {
            System.err.println(e.getMessage() + " | " + Arrays.toString(e.getStackTrace()));
            return e.getMessage();
        }
    }

    public List<Pair<Integer, EventEntry>> getIndexedEvents() {
        final List<EventEntry> allEvents = eventManager.getAllEvents();
        return IntStream.range(0, allEvents.size())
                .mapToObj(i -> Pair.of(i, allEvents.get(i))).toList();
    }

    public String manageEvents(String input) {
        List<Pair<Integer, EventEntry>> events = getIndexedEvents();
        List<Pair<Integer, EventEntry>> filtered = events;
        String cmds = """
                \n\nActions:
                  new
                  update <index>
                  delete <index>
                  view <index>
                  filter date (opens date prompt)
                  filter name <name>
                  filter tag <tag>
                  filter all
                  done
                """;

        String output = "";
        try {
            while (true) {
                clearAndPrint(TableUtil.generateTable(filtered, TableConfig.EVENT_MANAGE_TABLE));
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
                        createNewEvent("");
                        events = getIndexedEvents();
                        filtered = events;
                    }

                    case String s when s.startsWith("delete") -> {
                        int index;
                        if (userInput.length < 2 || (index = validateIndexInput(userInput[1], events)) == -1) {
                            output = "Invalid input or index";
                            continue;
                        }
                        EventEntry event = events.get(index).second();
                        boolean confirm = confirmPrompt(String.format("Delete event \"%s\"", event.name()));
                        if (confirm) {
                            eventManager.deleteEvent(event.uuid());
                            filtered.remove(events.get(index));
                            events.remove(index);
                            output = "Deleted: " + event.name();
                        }
                    }

                    case String s when s.startsWith("update") -> {
                        int index;
                        if (userInput.length < 2 || (index = validateIndexInput(userInput[1], events)) == -1) {
                            output = "Invalid input or index";
                            continue;
                        }
                        updateEvent(events.get(index).second());
                        output = "Updated: " + events.get(index).second().name();
                    }

                    case String s when s.startsWith("view") -> {
                        int index;
                        if (userInput.length < 2 || (index = validateIndexInput(userInput[1], events)) == -1) {
                            output = "Invalid input or index";
                            continue;
                        }
                        List<ColumnData<Pair<String, String>>> columns = TableUtil.createKeyPairColumns("", "");
                        clearAndPrint(TableUtil.generateTable(events.get(index).second().updateBuilder().toTableState(), columns));
                        promptInput("Press enter to continue");
                    }

                    case String s when s.startsWith("filter") -> {
                        if (userInput.length < 2) {
                            output = "Invalid input or index";
                            continue;
                        }
                        switch (userInput[1]) {
                            case String s1 when s1.startsWith("date") -> {
                                LocalDate start = promptDate("filter start");
                                LocalDate end = promptDate("filter end");
                                filtered = events.stream()
                                        .filter(e -> e.second().startTime().isAfter(start.atStartOfDay().minusDays(1))
                                                && e.second().endTime().isBefore(end.atStartOfDay().plusDays(1))
                                        ).toList();
                                output = "Filtered";
                            }
                            case String s1 when s1.startsWith("tag") -> {
                                if (userInput.length < 3) {
                                    output = "Invalid input or index";
                                    continue;
                                }
                                filtered = events.stream()
                                        .filter(e -> e.second().tags().contains(userInput[2]))
                                        .toList();
                                output = "Filtered";
                            }
                            case String s1 when s1.startsWith("name") -> {
                                if (userInput.length < 3) {
                                    printLnToTerminal("Invalid input");
                                }
                                filtered = events.stream()
                                        .filter(e -> e.second().name().toLowerCase().contains(userInput[2].toLowerCase()))
                                        .toList();
                                output = "Filtered";
                            }
                            case String s1 when s1.startsWith("all") -> {
                                filtered = events;
                                output = "Showing All";
                            }

                            default -> output = "Invalid input";
                        }
                    }

                    default -> output = "Invalid input";
                }
            }
        } catch (Exception e) {
            System.err.println(Arrays.toString(e.getStackTrace()));
        }
        return modeDisplay();
    }


}
