package shell.evaluators;

import application.App;
import com.github.freva.asciitable.ColumnData;
import entries.event.EventEntry;
import entries.event.EventManager;
import enums.NotificationLevel;
import io.mindspice.mindlib.data.tuples.Pair;
import shell.ShellCommand;
import util.TableUtil;
import util.Util;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;


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

    }

    public String createNewEvent(String input) {
        EventEntry.Builder eventBuilder = EventEntry.builder();
        var inputList = new ArrayList<Pair<String, String>>(8);

        List<ColumnData<Pair<String, String>>> columns = List.of(
                TableUtil.createColumn("Field", Pair::first),
                TableUtil.createColumn("Data", Pair::second)
        );

        Consumer<String> namePrompt = eventBuilder::setName;
        Consumer<String> notifyLvlPrompt = (i) -> eventBuilder.setNotificationLevel(NotificationLevel.fromString(i));

        try {
            terminal.output().write("New Event".getBytes());

            //Name
            promptInput("Name", "Event Name: ", eventBuilder::setName);
            clearAndPrint(TableUtil.generateTable(eventBuilder.toTableState(), columns) + " \n");

            //Start Time
            LocalDateTime start = promptDateTime("Start Time");
            if (start == null) { return "Aborted..."; }
            eventBuilder.setStartTime(start);
            clearAndPrint(TableUtil.generateTable(eventBuilder.toTableState(), columns) + " \n");

            //End time
            LocalDateTime end = promptDateTime("End Time");
            if (end == null) { return "Aborted..."; }
            eventBuilder.setStartTime(end);
            clearAndPrint(TableUtil.generateTable(eventBuilder.toTableState(), columns) + " \n");

            //Tags
            List<String> tags = promptTags("Event tags");
            eventBuilder.setTags(tags);
            clearAndPrint(TableUtil.generateTable(eventBuilder.toTableState(), columns) + " \n");

            //Reminder
            List<LocalDateTime> reminder = promptReminder("Event Reminders", eventBuilder.startTime);
            eventBuilder.setReminders(reminder);
            clearAndPrint(TableUtil.generateTable(eventBuilder.toTableState(), columns) + " \n");

            //Notification Level
            NotificationLevel notificationLevel;
            do {
                String userInput = promptInput("Notification Level",
                        "Notification Level (low, normal, critical): "
                );
                notificationLevel = Util.enumMatch(NotificationLevel.values(), userInput);
            } while (notificationLevel == null);

            eventBuilder.setNotificationLevel(notificationLevel);
            eventManger.addEvent(eventBuilder.build());
            clearAndPrint(TableUtil.generateTable(eventBuilder.toTableState(), columns) + " \n");
            return "!!!Event Saved!!!";

        } catch (IOException e) {
            System.err.println(e);
            return e.getMessage();
        }
    }

}
