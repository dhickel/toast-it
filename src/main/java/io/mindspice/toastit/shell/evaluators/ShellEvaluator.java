package io.mindspice.toastit.shell.evaluators;

import io.mindspice.mindlib.data.tuples.Pair;
import io.mindspice.toastit.entries.DatedEntry;
import io.mindspice.toastit.enums.NotificationLevel;
import io.mindspice.toastit.notification.Reminder;
import io.mindspice.toastit.shell.InputPrompt;
import io.mindspice.toastit.util.DateTimeUtil;
import io.mindspice.toastit.util.TableUtil;
import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;
import org.jline.utils.InfoCmp;
import io.mindspice.toastit.shell.ShellCommand;
import io.mindspice.toastit.util.Settings;
import io.mindspice.toastit.util.Util;

import java.io.IOException;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public abstract class ShellEvaluator<T> {
    public Terminal terminal;
    public LineReader lineReader;
    public List<ShellCommand<T>> commands = new ArrayList<>();

    public abstract String modeDisplay();

    public void init(Terminal terminal, LineReader reader) {
        this.terminal = terminal;
        this.lineReader = reader;
    }

    public String eval(String input) throws IOException {
        for (var cmd : commands) {
            if (cmd.match(input)) {
                try {
                    @SuppressWarnings("unchecked")
                    T self = (T) this;
                    return cmd.eval(self, input);
                } catch (Exception e) {

                    System.err.printf("Error evaluating in class: %s, Error: %s%n", this.getClass(), Arrays.toString(e.getStackTrace()));
                    e.printStackTrace();
                    return "Exception encountered while executing command: " + e.getMessage();
                }
            }
        }
        return "Invalid command or input";
    }

    public boolean replaceAlias(String oldAlias, String newAlias) {
        for (var cmd : commands) {
            if (cmd.aliases().contains(oldAlias)) {
                return cmd.replaceAlias(oldAlias, newAlias);
            }
        }
        return false;
    }

    public boolean addAlias(String existingAlias, String newAlias) {
        for (var cmd : commands) {
            if (cmd.aliases().contains(existingAlias)) {
                cmd.addAlias(newAlias);
                return true;
            }
        }
        return false;
    }

    public boolean removeAlias(String aliasToRemove) {
        for (var cmd : commands) {
            if (cmd.aliases().contains(aliasToRemove)) {
                return cmd.removeAlias(aliasToRemove);
            }
        }
        return false;
    }

    public String printActions() {
        return "Actions:\n" + commands.stream()
                .map(c -> String.format("  %s", String.join(" | ", c.aliases())))
                .collect(Collectors.joining("\n")) + "\n";
    }

    public void clearAndPrint(String s) {
        try {
            terminal.puts(InfoCmp.Capability.clear_screen);
            terminal.flush();
            terminal.output().write(s.getBytes());
            terminal.flush();
        } catch (IOException e) {
            System.err.printf("Error writing to terminal %s", s);
        }
    }

    public void clearScreen() {
        terminal.puts(InfoCmp.Capability.clear_screen);
        terminal.flush();
    }

    public void printLnToTerminal(String s) {
        try {
            terminal.output().write((s + "\n").getBytes());
            terminal.flush();
        } catch (IOException e) {
            System.err.printf("Error writing to terminal %s", s);
        }
    }

    public void promptInputConfirm(String prompt, String confirmString, Consumer<String> onPrompt) {
        String input = lineReader.readLine(prompt);

        boolean confirm = confirmPrompt("Confirm " + String.format("%s: \"%s\"", confirmString, input));
        if (!confirm) {
            promptInputConfirm(prompt, confirmString, onPrompt);
        }
        onPrompt.accept(input);
    }

    public void promptInputConfirm(String prompt, String confirmString, BiConsumer<T, String> onPrompt) {
        String input = lineReader.readLine(prompt);

        boolean confirm = confirmPrompt("Confirm " + String.format("%s: \"%s\"", confirmString, input));
        if (!confirm) {
            promptInputConfirm(prompt, confirmString, onPrompt);
        }
        try {
            @SuppressWarnings("unchecked")
            T self = (T) this;
            onPrompt.accept(self, input);
        } catch (Exception e) {
            System.err.printf("Error consuming input in class: %s, Error: %s%n", this.getClass(), e);
        }

    }

    public void promptInput(String prompt, Consumer<String> onPrompt) {
        String input = lineReader.readLine(prompt);
        onPrompt.accept(input);
    }

    public void promptInput(String prompt, BiConsumer<T, String> onPrompt) {
        String input = lineReader.readLine(prompt);
        try {
            @SuppressWarnings("unchecked")
            T self = (T) this;
            onPrompt.accept(self, input);
        } catch (Exception e) {
            System.err.printf("Error consuming input in class: %s, Error: %s%n", this.getClass(), e);
        }
    }

    public String promptInput(String prompt) {
        return lineReader.readLine(prompt);
    }

    public String promptInputConfirm(String prompt, String confirmString) {
        String input = lineReader.readLine(prompt);

        boolean confirm = confirmPrompt("Confirm " + String.format("%s: \"%s\"", confirmString, input));
        if (!confirm) {
            return promptInputConfirm(prompt, confirmString);
        }
        return input;
    }

    public LocalDateTime promptDateTime(String promptText) {
        LocalDate date = promptDate(promptText);
        LocalTime time = promptTime(promptText);

        LocalDateTime ldt = LocalDateTime.of(date, time);
        boolean confirm = confirmPrompt("Confirm " + DateTimeUtil.printDateTimeFull(ldt));
        if (confirm) {
            return ldt;
        } else {
            return promptDateTime(promptText);
        }
    }

    public LocalDate promptDate(String promptText) {
        while (true) {
            try {
                String input = lineReader.readLine(String.format("Enter %s Date: ", promptText)).trim();
                return DateTimeUtil.parseDateInput(input);
            } catch (DateTimeException e) {
                printLnToTerminal(String.format("Invalid input expected: %s", Settings.DATE_INPUT_PATTERNS));
            }
        }
    }

    public LocalTime promptTime(String promptName) {
        while (true) {
            try {
                String input = lineReader.readLine(String.format("Enter %s Time: ", promptName)).trim();
                return DateTimeUtil.parseTimeInput(input);
            } catch (DateTimeException e) {
                printLnToTerminal(String.format("Invalid input expected: %s", Settings.TIME_INPUT_PATTERNS));
            }
        }
    }

    public List<Reminder> promptReminder(LocalDateTime eventTime) {
        List<Reminder> reminders = new ArrayList<>(2);

        while (true) {
            String input = lineReader.readLine("Enter Reminder Interval: ").trim();

            String[] splitInput = input.split(" ");
            if (splitInput.length < 2 || !Util.isInt(splitInput[0])) {
                printLnToTerminal("Invalid Input, format: <#> <min/hour/day/week/month> ");
                continue;
            }

            int interval = Integer.parseInt(splitInput[0]);
            LocalDateTime rTime;
            switch (splitInput[1].toLowerCase()) {
                case String s when s.startsWith("month") -> rTime = eventTime.minusMonths(interval).truncatedTo(ChronoUnit.MINUTES);
                case String s when s.startsWith("week") -> rTime = eventTime.minusWeeks(interval).truncatedTo(ChronoUnit.MINUTES);
                case String s when s.startsWith("day") -> rTime = eventTime.minusDays(interval).truncatedTo(ChronoUnit.MINUTES);
                case String s when s.startsWith("hour") -> rTime = eventTime.minusHours(interval).truncatedTo(ChronoUnit.MINUTES);
                case String s when s.startsWith("min") -> rTime = eventTime.minusMinutes(interval).truncatedTo(ChronoUnit.MINUTES);
                default -> {
                    printLnToTerminal("Invalid Input, Valid intervals: \"# day\", \"# hour\", \"# min\", \"# week\",, \"# month\", \"exit\" ex. \"30 min\" ");
                    continue;
                }
            }

            NotificationLevel nLevel;
            do {
                String userInput = promptInput("Notification Level (low, normal, critical): ");
                nLevel = Util.enumMatch(NotificationLevel.values(), userInput);
                if (nLevel == null) {
                    printLnToTerminal("Invalid Input");
                }
            } while (nLevel == null);

            if (confirmPrompt(
                    String.format("Confirm Reminder: %s | %s", DateTimeUtil.printDateTimeFull(rTime), nLevel))
            ) {
                reminders.add(new Reminder(rTime, nLevel));
            }
            if (!confirmPrompt("Add another reminder?")) {
                return reminders;
            }
        }
    }

    public List<String> promptNotes() throws IOException {
        List<String> notes = new ArrayList<>(4);
        do {
            String table = TableUtil.generateKeyPairTable(
                    "Notes", notes, (__) -> "Note ",
                    (note) -> TableUtil.wrapString(note, Settings.TABLE_MAX_COLUMN_WIDTH - 4)
            );

            clearAndPrint(table + "\n");
            notes.add(simpleTextEdit(""));

            table = TableUtil.generateKeyPairTable(
                    "Notes", notes, (__) -> "Note ",
                    (note) -> TableUtil.wrapString(note, Settings.TABLE_MAX_COLUMN_WIDTH - 4)
            );

            clearAndPrint(table + "\n");
        } while (confirmPrompt("Add another note?"));
        return notes;
    }

    public List<String> updateNotes(List<String> notes) throws IOException {
        String cmds = String.join("\n", "\nAvailable Actions:",
                TableUtil.basicRow(2, "new", "edit <index>", "delete <index>", "done")
        );
        InputPrompt<String> prompt = new InputPrompt<>(notes);

        String output = "";

        while (true) {
            String table = TableUtil.generateIndexedPairTable("Notes", "Note", prompt.getItems(), (note) -> note);
            clearAndPrint(table + "\n");

            printLnToTerminal(cmds);
            if (!output.isEmpty()) {
                printLnToTerminal(output + "\n");
                output = "";
            }

            String[] input = promptInput("Action: ").trim().split(" ");
            switch (input[0]) {

                case String s when s.startsWith("new") -> prompt.addItem(simpleTextEdit(""));

                case String s when s.startsWith("done") -> {
                    clearScreen();
                    return prompt.getItems();
                }

                case String s when s.startsWith("delete") -> output = prompt.create()
                        .validateInputLength(input, 2)
                        .validateAndGetIndex(input[1])
                        .confirm(this::confirmPrompt, entry -> "Delete Note: " + input[0])
                        .listRemove()
                        .display(entry -> "Deleted: " + input[0]);

                case String s when s.startsWith("edit") -> output = prompt.create()
                        .validateInputLength(input, 2)
                        .validateAndGetIndex(input[1])
                        .itemUpdate(stringEntryUpdater)
                        .display(entry -> "Updated: Note" + input[0]);

                default -> output = "Invalid input or index";
            }
        }
    }

    public List<String> promptTags(String header) {
        List<String> tags = new ArrayList<>(4);

        printLnToTerminal(header);
        String input = lineReader.readLine("Enter Tags(seperated by spaces): ").trim();

        String[] inputTags = input.split("\\s+");
        for (var tag : inputTags) {
            if (!Settings.TAG_MAP.containsKey(input)) {
                boolean confirm = confirmPrompt(String.format("Tag: %s unknown include anyway?", tag));
                if (confirm) {
                    tags.add(tag);
                }
            } else {
                tags.add(tag);
            }
        }
        return tags;
    }

    public boolean confirmPrompt(String prompt) {
        while (true) {
            String input = lineReader.readLine(prompt + (prompt.contains("(y/n)")
                                                         ? ""
                                                         : " (y/n): ")).trim().toLowerCase();

            if (input.startsWith("n") || input.startsWith("exit")) {
                return false;
            }
            if (input.startsWith("y")) {
                return true;
            } else {
                printLnToTerminal("Invalid Input.");
            }
        }
    }

    public boolean isDone(String input) {
        return input.equalsIgnoreCase("'d")
                || input.equalsIgnoreCase("`d")
                || input.equalsIgnoreCase("'done")
                || input.equalsIgnoreCase("`done")
                || input.equalsIgnoreCase("exit");
    }

    public int validateIndexInput(String input, List<?> list) {
        if (!Util.isInt(input)) { return -1; }
        int index = Integer.parseInt(input);
        if (index < 0 || index >= list.size()) { return -1; }
        return index;
    }

    public <U extends DatedEntry> String filterPrompt(String[] userInput, InputPrompt<U> prompt) {
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

            case String s1 when s1.startsWith("created") -> {
                LocalDateTime start = promptDate("filter start").atStartOfDay();
                LocalDateTime end = promptDate("filter end").plusDays(1).atStartOfDay();
                return prompt.create()
                        .filter(t -> t.startedAt().isAfter(start) && t.startedAt().isBefore(end))
                        .display(__ -> String.format("Filtered Created By: %s - %s", start, end));
            }

            case String s1 when s1.startsWith("start") -> {
                LocalDateTime start = promptDate("filter start").atStartOfDay();
                LocalDateTime end = promptDate("filter end").plusDays(1).atStartOfDay();
                return prompt.create()
                        .filter(t -> t.startedAt().isAfter(start) && t.startedAt().isBefore(end))
                        .display(__ -> String.format("Filtered Started At: %s - %s", start, end));
            }

            case String s1 when s1.startsWith("completed") -> {
                return prompt.create().filter(DatedEntry::completed).display(__ -> "Filter Completed");
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

    public <U extends DatedEntry> int archiveEntries(Predicate<Pair<Integer, U>> predicate,
            InputPrompt<U> prompt, Consumer<U> archiveConsumer) {
        List<U> archived = prompt.getIndexedItems().stream().filter(predicate).map(Pair::second).toList();
        archived.forEach(archiveConsumer);
        prompt.removeItems(predicate);
        return archived.size();
    }

    public <U> List<Pair<Integer, U>> getIndexedList(final List<U> items) {
        return IntStream.range(0, items.size())
                .mapToObj(i -> Pair.of(i, items.get(i))).collect(Collectors.toList());
    }

    public UnaryOperator<String> stringEntryUpdater = (note) -> {
        try {
            return Util.tempEdit(Util.nanoInstance(terminal), note);
        } catch (IOException e) {
            printLnToTerminal("Error opening nano to edit: " + e.getMessage());
            System.err.println(e.getMessage() + " | " + Arrays.toString(e.getStackTrace()));
            return note;
        }
    };

    public String simpleTextEdit(String existing) {
        try {
            return Util.tempEdit(Settings.getEditorOr(Settings.SIMPLE_TEXT_EDITOR), existing);
        } catch (IOException e) {
            printLnToTerminal("Error opening editor: " + e.getMessage());
            System.err.println(e.getMessage() + " | " + Arrays.toString(e.getStackTrace()));
            return "";
        }
    }

    public void showDataView(String header, String data) {
        clearAndPrint(TableUtil.basicColumn(header, data));
        promptInput("Press Enter To Continue...");
    }

}
