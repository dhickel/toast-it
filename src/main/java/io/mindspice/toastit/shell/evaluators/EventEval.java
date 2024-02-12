package io.mindspice.toastit.shell.evaluators;

import io.mindspice.toastit.App;
import com.github.freva.asciitable.ColumnData;
import io.mindspice.toastit.entries.event.EventEntry;
import io.mindspice.toastit.entries.event.EventManager;
import io.mindspice.toastit.enums.NotificationLevel;
import io.mindspice.mindlib.data.tuples.Pair;
import io.mindspice.toastit.shell.ShellCommand;
import io.mindspice.toastit.util.TableConfig;
import io.mindspice.toastit.util.TableUtil;
import io.mindspice.toastit.util.Util;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;


public class EventEval extends ShellEvaluator<EventEval> {
    public final EventManager eventManger = App.instance().getEventManager();

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
                ShellCommand.of(Set.of("manage", "manager"), EventEval::manageEvents)
        ));
    }

    public String createNewEvent(String input) {
        EventEntry.Builder eventBuilder = EventEntry.builder();

        List<ColumnData<Pair<String, String>>> columns = TableUtil.createKeyPairColumns("", "");
        try {
            clearScreen();
            printLnToTerminal(TableUtil.basicBox("New event"));

            //Name
            eventBuilder.name = promptInput("Enter Event Name: ");
            System.out.println(eventBuilder.toTableState());
            clearAndPrint(TableUtil.generateTable(eventBuilder.toTableState(), columns) + "\n");

            //Start Time
            LocalDateTime start = promptDateTime("Start");
            if (start == null) { return "Aborted..."; }
            eventBuilder.startTime = start;
            clearAndPrint(TableUtil.generateTable(eventBuilder.toTableState(), columns) + "\n");

            //End time
            LocalDateTime end = promptDateTime("End");
            if (end == null) { return "Aborted..."; }
            eventBuilder.endTime = end;
            clearAndPrint(TableUtil.generateTable(eventBuilder.toTableState(), columns) + "\n");

            //Tags
            eventBuilder.tags = promptTags("Event tags");
            clearAndPrint(TableUtil.generateTable(eventBuilder.toTableState(), columns) + "\n");

            //Reminder
            eventBuilder.reminders = promptReminder("Event Reminders", eventBuilder.startTime);
            clearAndPrint(TableUtil.generateTable(eventBuilder.toTableState(), columns) + "\n");

            //Notification Level
            NotificationLevel notificationLevel;
            do {
                String userInput = promptInput("Notification Level (low, normal, critical): ");
                notificationLevel = Util.enumMatch(NotificationLevel.values(), userInput);
            } while (notificationLevel == null);

            eventBuilder.notificationLevel = notificationLevel;
            eventManger.addEvent(eventBuilder.build());
            clearAndPrint(TableUtil.generateTable(eventBuilder.toTableState(), columns) + " \n");
            return TableUtil.basicBox("Saved Event");

        } catch (IOException e) {
            System.err.println(e);
            return e.getMessage();
        }
    }

    public String updateEvent(EventEntry event) {
        EventEntry.Builder eventBuilder = event.updateBuilder();
        List<ColumnData<Pair<String, String>>> columns = TableUtil.createKeyPairColumns("", "");

        try {
            clearAndPrint(TableUtil.generateTable(eventBuilder.toTableState(), columns) + " \n");
            if (confirmPrompt("Update Name?")) {
                eventBuilder.name = promptInput("Enter New Name: ");
            }

            clearAndPrint(TableUtil.generateTable(eventBuilder.toTableState(), columns) + " \n");
            if (confirmPrompt("Update Start Time?")) {
                eventBuilder.startTime = promptDateTime("New Start");
            }

            clearAndPrint(TableUtil.generateTable(eventBuilder.toTableState(), columns) + " \n");
            if (confirmPrompt("Update End Time?")) {
                eventBuilder.endTime = promptDateTime("New End");
            }

            clearAndPrint(TableUtil.generateTable(eventBuilder.toTableState(), columns) + " \n");
            if (confirmPrompt("Update Tags?")) {
                eventBuilder.tags = promptTags("New Event Tags");
            }

            clearAndPrint(TableUtil.generateTable(eventBuilder.toTableState(), columns) + " \n");
            if (confirmPrompt("Update Reminders?")) {
                eventBuilder.reminders = promptReminder("New Event Reminders", eventBuilder.startTime);
            }

            clearAndPrint(TableUtil.generateTable(eventBuilder.toTableState(), columns) + " \n");
            if (confirmPrompt("Update Notification Level?")) {
                NotificationLevel notificationLevel;
                do {
                    String userInput = promptInput("New Notification Level (low, normal, critical): ");
                    notificationLevel = Util.enumMatch(NotificationLevel.values(), userInput);
                } while (notificationLevel == null);

                eventBuilder.notificationLevel = notificationLevel;
            }

            eventManger.updateEvent(eventBuilder.build());
            clearAndPrint(TableUtil.generateTable(eventBuilder.toTableState(), columns) + " \n");
            return TableUtil.basicBox("Updated Event");

        } catch (IOException e) {
            System.err.println(e.getMessage() + " | " + Arrays.toString(e.getStackTrace()));
            return e.getMessage();
        }
    }

    public List<Pair<Integer, EventEntry>> getIndexedEvents() {
        final List<EventEntry> allEvents = eventManger.getAllEvents();
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
                  filter date (brings up date prompt)
                  filter name <name>
                  filter tag <tag>
                  filter all
                  exit\n
                """;

        String output = "";
        try {
            while (true) {
                clearAndPrint(TableUtil.generateTable(filtered, TableConfig.EVENT_EDIT_TABLE));
                printLnToTerminal(cmds);
                if (!output.isEmpty()) {
                    printLnToTerminal("> " + output + "\n");
                    output = "";
                }

                String[] userInput = promptInput("Action: ").trim().split(" ");
                switch (userInput[0]) {
                    case String s when s.startsWith("exit") -> { return "Exited Event Manager"; }
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
                            eventManger.deleteEvent(event.uuid());
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
                                }
                                filtered = events.stream().filter(e -> e.second().tags().contains(userInput[2])).toList();
                                output = "Filtered";
                            }
                            case String s1 when s1.startsWith("name") -> {
                                if (userInput.length < 3) {
                                    printLnToTerminal("Invalid input");
                                }
                                filtered = events.stream().filter(e -> e.second().name().contains(userInput[2])).toList();
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
            output = "Error: " + e.getMessage();
        }
        return "Exited Manager";
    }


}