package io.mindspice.toastit.shell.evaluators;

import io.mindspice.toastit.enums.NotificationLevel;
import io.mindspice.toastit.notification.Reminder;
import io.mindspice.toastit.util.DateTimeUtil;
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
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;


public abstract class ShellEvaluator<T> {
    public Terminal terminal;
    public LineReader lineReader;
    public List<ShellCommand<T>> commands = new ArrayList<>();
    public Supplier<String> modeDisplay = this::modeDisplay;

    protected abstract String modeDisplay();

    public void setModeDisplay(Supplier<String> displaySupplier) {
        modeDisplay = displaySupplier;
    }

    public void init(Terminal terminal, LineReader reader) {
        this.terminal = terminal;
        this.lineReader = reader;
    }

    public String eval(String input) {
        for (var cmd : commands) {
            if (cmd.match(input)) {
                try {
                    @SuppressWarnings("unchecked")
                    T self = (T) this;
                    return cmd.eval(self, input);
                } catch (Exception e) {
                    System.err.printf("Error evaluating in class: %s, Error: %s%n", this.getClass(), e);
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

    public void clearAndPrint(String s) throws IOException {
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

    // TODO add BiConsumer for external user methods

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

    public List<Reminder> promptReminder(String header, LocalDateTime eventTime) {
        List<Reminder> reminders = new ArrayList<>(2);
        List<String> valid = List.of("day", "days", "hr", "hour", "hours", "min", "minute", "minutes");

        printLnToTerminal(header);
        while (true) {
            String input = lineReader.readLine("Enter Reminder Interval('d when done): ").trim();
            if (isDone(input)) {
                return reminders;
            }

            String[] splitInput = input.split(" ");
            System.out.println(Arrays.toString(splitInput));
            if (splitInput.length < 2 || !Util.isInt(splitInput[0]) || !valid.contains(splitInput[1].toLowerCase())) {
                System.out.println();
                printLnToTerminal("Invalid Input, Valid intervals: \"# day\", \"# hr\", \"# min\" \"exit\" ex. \"30 min\" ");
                continue;
            }

            int interval = Integer.parseInt(splitInput[0]);
            LocalDateTime rTime;
            switch (splitInput[1].toLowerCase()) {
                case String s when s.startsWith("day") -> rTime = eventTime.minusDays(interval).truncatedTo(ChronoUnit.MINUTES);
                case String s when s.startsWith("hr") || s.startsWith("hour") ->
                        rTime = eventTime.minusHours(interval).truncatedTo(ChronoUnit.MINUTES);
                case String s when s.startsWith("min") -> rTime = eventTime.minusMinutes(interval).truncatedTo(ChronoUnit.MINUTES);
                default -> {
                    printLnToTerminal("Invalid Input, Valid intervals: \"# day\", \"# hr\", \"# min\" \"exit\" ex. \"30 min\" ");
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

            boolean confirm = confirmPrompt(
                    String.format("Confirm Reminder: %s | %s", DateTimeUtil.printDateTimeFull(rTime), nLevel)
            );
            if (confirm) {
                reminders.add(new Reminder(rTime, nLevel));
            }
        }
    }

    public List<String> promptTags(String header) {
        List<String> tags = new ArrayList<>(4);

        printLnToTerminal(header);
        while (true) { // TODO add completion
            String input = lineReader.readLine("Enter Tag('d when done): ").trim();
            if (isDone(input)) {
                return tags;
            }
            if (!Settings.TAG_MAP.containsKey(input)) {
                boolean confirm = confirmPrompt("Unknown tag, add anyway?");
                if (confirm) {
                    tags.add(input);
                }
            }
        }
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
}
