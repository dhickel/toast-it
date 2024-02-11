package shell.evaluators;

import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;
import org.jline.utils.InfoCmp;
import shell.ShellCommand;
import util.Settings;
import util.Util;

import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;


public abstract class ShellEvaluator<T> {
    public Terminal terminal;
    public LineReader lineReader;
    public List<ShellCommand<T>> commands = new ArrayList<>();

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

    public void printLnToTerminal(String s) {
        try {
            terminal.output().write((s + "\n").getBytes());
            terminal.flush();
        } catch (IOException e) {
            System.err.printf("Error writing to terminal %s", s);
        }
    }

    public void promptInput(String header, String prompt, Consumer<String> onPrompt) {
        printLnToTerminal(header);
        String input = lineReader.readLine(prompt);

        boolean confirm = confirmPrompt("Confirm " + header);
        if (!confirm) {
            promptInput(header, prompt, onPrompt);
        }
        onPrompt.accept(input);
    }

    public String promptInput(String header, String prompt) {
        printLnToTerminal(header);
        String input = lineReader.readLine(prompt);

        boolean confirm = confirmPrompt("Confirm " + header);
        if (!confirm) {
            return promptInput(header, prompt);
        }
    }

    // TODO add BiConsumer for external user methods

    public LocalDateTime promptDateTime(String header) {
        LocalDate date;
        LocalTime time;

        while (true) {
            try {
                printLnToTerminal(header);
                String input = lineReader.readLine("Enter Date: ").trim();
                if (input.equalsIgnoreCase("exit")) {
                    return null;
                }
                date = Util.parseDateInput(input);
                break;
            } catch (DateTimeException e) {
                printLnToTerminal(String.format("Invalid input expected: %s", Settings.DATE_INPUT_FORMAT));
            }
        }

        while (true) {
            try {
                String input = lineReader.readLine("Enter Time(12hr or 24hr): ").trim();
                if (input.equalsIgnoreCase("exit")) {
                    return null;
                }
                time = Util.parseTimeInput(input);
                break;
            } catch (DateTimeParseException e) {
                printLnToTerminal("Invalid input expected: HH:mm, h:mm a or h:mma | Ex 20:30, 8:30pm, 8:30 pm");
            }
        }

        boolean confirm = confirmPrompt("Confirm " + header);
        if (confirm) {
            return LocalDateTime.of(date, time);
        } else {
            return promptDateTime(header);
        }
    }

    public List<LocalDateTime> promptReminder(String header, (LocalDateTime eventTime) {
        List<LocalDateTime> reminders = new ArrayList<>(2);
        List<String> valid = List.of("day, hr, min");

        while (true) {
            printLnToTerminal(header);
            printLnToTerminal("Valid intervals: \"# day\", \"# hr\", \"# min\" \"exit\" ex. \"30 min\" ");
            String input = lineReader.readLine("Enter Reminder Interval: ").trim();
            if (input.equalsIgnoreCase("exit")) {
                return reminders;
            }
            String[] splitInput = input.split(" ");
            if (splitInput.length < 2 || !Util.isInt(splitInput[0]) || !valid.contains(splitInput[1].toLowerCase())) {
                printLnToTerminal("Invalid Input");
                continue;
            }

            int interval = Integer.parseInt(splitInput[0]);
            switch (splitInput[1].toLowerCase()) {
                case String s when s.startsWith("day") -> {
                    LocalDateTime dt = eventTime.minusDays(interval).truncatedTo(ChronoUnit.MINUTES);
                    boolean confirm = confirmPrompt(String.format("Confirm Reminder: %s", dt.toString());
                    if (confirm) { reminders.add(dt); }
                }
                case String s when s.startsWith("hr") || s.startsWith("hour") -> {
                    LocalDateTime dt = eventTime.minusHours(interval);
                    boolean confirm = confirmPrompt(String.format("Confirm Reminder: %s", dt.toString());
                    if (confirm) { reminders.add(dt); }
                }
                case String s when s.startsWith("min") -> {
                    LocalDateTime dt = eventTime.minusMinutes(interval);
                    boolean confirm = confirmPrompt(String.format("Confirm Reminder: %s", dt.toString());
                    if (confirm) { reminders.add(dt); }
                }
                default -> {
                    printLnToTerminal("Invalid Input");
                    continue;
                }
            }

            boolean addMore = confirmPrompt("Add Another Reminder?");
            if (!addMore) {
                return reminders;
            }
        }
    }

    public List<String> promptTags(String header) {
        List<String> tags = new ArrayList<>(4);

        printLnToTerminal(header);
        while (true) { // TODO add completion
            String input = lineReader.readLine("Enter Tag: ").trim();

            if (!Settings.TAG_MAP.containsKey(input)) {
                boolean confirm = confirmPrompt("Unknown tag, add anyway?");
                if (confirm) {
                    tags.add(input);
                } else {
                    continue;
                }
            }

            boolean addMore = confirmPrompt("Add Another Tag?");
            if (!addMore) {
                return tags;
            }
        }
    }

    public boolean confirmPrompt(String prompt) {
        while (true) {
            String input = lineReader.readLine(prompt + (prompt.contains("(y|n)")
                    ? ""
                    : " (y|n):").trim().toLowerCase()
            );

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
}
