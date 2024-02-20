package io.mindspice.toastit.shell.evaluators;

import com.github.freva.asciitable.ColumnData;
import io.mindspice.toastit.App;
import io.mindspice.toastit.entries.SearchResult;
import io.mindspice.toastit.entries.text.TextEntry;
import io.mindspice.toastit.entries.text.TextManager;
import io.mindspice.toastit.enums.EntryType;
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
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;


public class TextEval extends ShellEvaluator<TextEval> {
    public final TextManager manager;
    public final String tStr;
    public final EntryType type;

    public TextEval(EntryType entryType) {
        if (entryType == EntryType.NOTE) {
            tStr = "Note";
            manager = App.instance().getNoteManager();
        } else {
            tStr = "Journal";
            manager = App.instance().getJournalManager();
        }
        type = entryType;
        initBaseCommands();
    }

    public void initBaseCommands() {
        commands.addAll(List.of(
                ShellCommand.of("new", TextEval::createNewEntry),
                ShellCommand.of("manage", TextEval::manageEntries),
                ShellCommand.of("open", TextEval::onOpenEntry),
                ShellCommand.of("view", TextEval::onViewEntry)
        ));
        if (type == EntryType.JOURNAL) {
            commands.add(ShellCommand.of("daily", TextEval::dailyJournal));
        }
    }

    @Override
    public String modeDisplay() {
        clearScreen();
        return TableConfig.TEXT_DASHBOARD_FORMATTER.apply(this) + "\n";
    }

    public String activeTextTable() {
        List<ColumnData<TextEntry>> viewColumns = TableConfig.TEXT_OVERVIEW_TABLE;
        String table = TableUtil.generateTableWithHeader("Active " + tStr, manager.getEntries(), viewColumns);
        String cmds = String.join("\n", "\nAvailable Actions:",
                (type == EntryType.JOURNAL
                 ? TableUtil.basicRow(2, "new", "manage", "open <name>", "view <name>")
                 : TableUtil.basicRow(2, "new", "manage", "open <name>", "view <name>, daily"))
        );

        return String.join("\n", table, cmds) + "\n";

    }

    public String onOpenEntry(String s) {
        TextEntry entry = Util.entryMatch(manager.getEntries(), Util.removeFirstWord(s));
        if (entry == null) {
            return "Entry Not Found";
        } else {
            Settings.getEditor(Settings.FULL_TEXT_EDITOR).accept(entry.getFilePath());
            return modeDisplay();
        }
    }

    public String onViewEntry(String s) {
        TextEntry entry = Util.entryMatch(manager.getEntries(), Util.removeFirstWord(s));
        if (entry == null) {
            return "Entry Not Found";
        } else {
            viewEntry(entry);
            return modeDisplay();
        }
    }

    public String dailyJournal(String s) {
        try {
            Settings.getEditor(Settings.FULL_TEXT_EDITOR).accept(manager.getDailyJournal().getFilePath());
        } catch (IOException e) {
            System.err.println("Error creating journal entry: " + e);
            return e.getMessage();
        }
        return modeDisplay();
    }

    public String createNewEntry(String s) {
        try {
            TextEntry.Builder noteBuilder = TextEntry.builder(EntryType.NOTE);

            noteBuilder.name = promptInput(String.format("Enter %s Name: ", tStr));
            noteBuilder.tags = promptTags(String.format("%s Tags", tStr));
            noteBuilder.createdAt = LocalDateTime.now();

            TextEntry note = noteBuilder.build();
            manager.addEntry(note);
            printLnToTerminal("Created " + tStr);

            if (confirmPrompt(String.format("Open %s Now?", tStr))) {
                Consumer<Path> editor = Settings.getEditor(Settings.FULL_TEXT_EDITOR);
                editor.accept(note.getFilePath());
            }
            promptInput(tStr + " Created, Press Enter To Return...");
            return modeDisplay();
        } catch (IOException e) {
            System.err.println(Arrays.toString(e.getStackTrace()));
            return e.getMessage();
        }
    }

    public TextEntry updateEntry(TextEntry entry) {
        try {
            TextEntry.Builder textBuilder = entry.updateBuilder();

            if (confirmPrompt("Update Name?")) {
                textBuilder.name = promptInput("Enter New Name: ");
            }
            if (confirmPrompt("Replace Tags?")) {
                textBuilder.tags = promptTags("New Tags");
            }
            return textBuilder.build();
        } catch (IOException e) {
            printLnToTerminal("Error updating item: " + e.getMessage());
            System.err.println(e);
            return entry;
        }
    }

    public String manageEntries(String input) {
        InputPrompt<TextEntry> textPrompt = new InputPrompt<>(manager.getEntries());

        String cmds = String.join("\n", "\nAvailable Actions:",
                TableUtil.basicRow(2, "new", "open <index/name>", "view <index/name>", "update <index/name>", "delete <index/name>"),
                TableUtil.basicRow(2, "filter<all>", "filter <tag>", "filter <name>", "filter created"),
                TableUtil.basicRow(2, "search <String>", "archive <index/name>", "done"));

        String output = "";
        while (true) {
            clearAndPrint(TableUtil.generateTableWithHeader(
                    "Manage " + tStr,
                    textPrompt.getFiltered(),
                    TableConfig.TEXT_MANAGE_TABLE)
            );
            printLnToTerminal(cmds);

            if (!output.isEmpty()) {
                printLnToTerminal("\n" + output + "\n");
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
                    createNewEntry("");
                    textPrompt = new InputPrompt<>(manager.getEntries());
                }

                case String s when s.startsWith("update") -> {
                    output = Util.isInt(userInput[1])
                             ? textPrompt.create()
                                     .validateInputLength(userInput, 2)
                                     .validateAndGetIndex(userInput[1])
                                     .itemUpdate(this::updateEntry)
                                     .itemConsumer(manager::updateEntry)
                                     .display(item -> "Updated: " + item.name())
                             : textPrompt.create()
                                     .validateInputLength(userInput, 2)
                                     .forceSelect(Util.entryMatch(textPrompt.getItems(), rawInput.replace("update", "")))
                                     .itemUpdate(this::updateEntry)
                                     .itemConsumer(manager::updateEntry)
                                     .display(item -> "Updated: " + item.name());
                }

                case String s when s.startsWith("delete") -> {
                    output = Util.isInt(userInput[1])
                             ? textPrompt.create()
                                     .validateInputLength(userInput, 2)
                                     .validateAndGetIndex(userInput[1])
                                     .confirm(this::confirmPrompt, item -> String.format("Delete %s: \"%s\"?", tStr, item.name()))
                                     .itemConsumer(manager::deleteEntry)
                                     .listRemove()
                                     .display(item -> "Deleted: " + item.name())
                             : textPrompt.create()
                                     .validateInputLength(userInput, 2)
                                     .forceSelect(Util.entryMatch(textPrompt.getItems(), rawInput.replace("delete", "")))
                                     .confirm(this::confirmPrompt, item -> String.format("Delete %s: \"%s\"?", tStr, item.name()))
                                     .itemConsumer(manager::deleteEntry)
                                     .listRemove()
                                     .display(item -> "Deleted: " + item.name());
                }

                case String s when s.startsWith("open") -> {
                    output = Util.isInt(userInput[1])
                             ? textPrompt.create()
                                     .validateInputLength(userInput, 2)
                                     .validateAndGetIndex(userInput[1])
                                     .itemConsumer(i -> Settings.getEditor(Settings.FULL_TEXT_EDITOR).accept(i.getFilePath()))
                                     .waitPrompt(() -> promptInput("Press Enter When Finished And Saved"))
                                     .display(__ -> "")
                             : textPrompt.create()
                                     .validateInputLength(userInput, 2)
                                     .forceSelect(Util.entryMatch(textPrompt.getItems(), rawInput.replace("open", "")))
                                     .itemConsumer(i -> Settings.getEditor(Settings.FULL_TEXT_EDITOR).accept(i.getFilePath()))
                                     .waitPrompt(() -> promptInput("Press Enter When Finished And Saved"))
                                     .display(__ -> "");
                }

                case String s when s.startsWith("view") -> {
                    output = Util.isInt(userInput[1])
                             ? textPrompt.create()
                                     .validateInputLength(userInput, 2)
                                     .validateAndGetIndex(userInput[1])
                                     .itemConsumer(this::viewEntry)
                                     .display(__ -> "")
                             : textPrompt.create()
                                     .validateInputLength(userInput, 2)
                                     .forceSelect(Util.entryMatch(textPrompt.getItems(), rawInput.replace("view", "")))
                                     .itemConsumer(this::viewEntry)
                                     .display(__ -> "");
                }

                case String s when s.startsWith("filter") -> filterPrompt(userInput, textPrompt);

                case String s when s.startsWith("search") && userInput.length > 1 -> {
                    List<SearchResult> results = manager.searchEntries(rawInput.replace("search", ""));
                    viewSearchResults(results);
                }

                case String s when s.startsWith("archive") -> {
                    output = Util.isInt(userInput[1])
                             ? textPrompt.create()
                                     .validateInputLength(userInput, 2)
                                     .validateAndGetIndex(userInput[1])
                                     .confirm(this::confirmPrompt, i -> String.format("Archive %s: %s ?", tStr, i.name()))
                                     .itemConsumer(manager::archiveEntry)
                                     .listRemove()
                                     .display(i -> "Archived: " + i.name())
                             : textPrompt.create()
                                     .validateInputLength(userInput, 2)
                                     .forceSelect(Util.entryMatch(textPrompt.getItems(), rawInput.replace("archive", "")))
                                     .itemConsumer(manager::archiveEntry)
                                     .listRemove()
                                     .display(i -> "Archived: " + i.name());
                }

                default -> output = "Invalid Input";
            }
        }
    }

    public void viewEntry(TextEntry entry) {
        try {
            clearAndPrint(TableUtil.generateTable(List.of(Files.readString(entry.getFilePath())), TableConfig.TEXT_VIEW));
        } catch (IOException e) {
            printLnToTerminal("Error opening file:" + entry.getFilePath());
        }
        promptInput("Press Enter To Return To Menu");
    }

    public void viewSearchResults(List<SearchResult> results) {
        InputPrompt<SearchResult> resultPrompt = new InputPrompt<>(results);
        String cmds = String.join("\n", "\nAvailable Actions:",
                TableUtil.basicRow(2, "open <index>", "view <index", "done"));

        String output = "";
        while (true) {
            clearAndPrint(TableUtil.generateTableWithHeader(
                    "Search Results",
                    resultPrompt.getIndexedItems(),
                    TableConfig.SEARCH_VIEW_TABLE)
            );

            printLnToTerminal(cmds);
            if (!output.isEmpty()) {
                printLnToTerminal("\n" + output + "\n");
                output = "";
            }

            String[] userInput = promptInput("Action: ").trim().split(" ");

            switch (userInput[0]) {
                case String s when s.startsWith("done") -> { return; }

                case String s when s.startsWith("view") -> output = resultPrompt.create()
                        .validateInputLength(userInput, 2)
                        .validateAndGetIndex(userInput[1])
                        .itemConsumer(sr -> viewEntry(sr.entry()))
                        .display(__ -> "");

                case String s when s.startsWith("open") -> output = resultPrompt.create()
                        .validateInputLength(userInput, 2)
                        .validateAndGetIndex(userInput[1])
                        .itemConsumer(i -> Settings.getEditor(Settings.FULL_TEXT_EDITOR).accept(i.entry().getFilePath()))
                        .display(__ -> "");

                default -> output = "Invalid input or index";

            }
        }

    }
}
