package io.mindspice.toastit.shell.evaluators;

import io.mindspice.toastit.App;
import io.mindspice.toastit.entries.SearchResult;
import io.mindspice.toastit.entries.text.TextEntry;
import io.mindspice.toastit.entries.text.TextEntryManager;
import io.mindspice.toastit.enums.EntryType;
import io.mindspice.toastit.shell.InputPrompt;
import io.mindspice.toastit.util.Settings;
import io.mindspice.toastit.util.TableConfig;
import io.mindspice.toastit.util.TableUtil;
import io.mindspice.toastit.util.Util;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;


public class TextEntryEval extends ShellEvaluator<TextEntryEval> {
    public final TextEntryManager manager;
    public final String tStr;
    public final EntryType type;

    public TextEntryEval(EntryType entryType) {
        if (entryType == EntryType.NOTE) {
            tStr = "Note";
            manager = App.instance().getNoteManager();
        } else {
            tStr = "Journal";
            manager = App.instance().getJournalManager();
        }
        type = entryType;
    }

    public TextEntry createNewEntry() throws IOException {
        TextEntry.Builder noteBuilder = TextEntry.builder(EntryType.NOTE);

        noteBuilder.name = promptInput(String.format("Enter %s Name: ", tStr));
        noteBuilder.tags = promptTags(String.format("%s Tags", tStr));
        noteBuilder.createdAt = LocalDateTime.now();

        TextEntry note = noteBuilder.build();
        manager.addEntry(note);
        printLnToTerminal("Created " + tStr);

        if (confirmPrompt(String.format("Open %s Now?", tStr))) {
            Consumer<Path> editor = Settings.getEditor(Settings.FULL_NOTE_EDITOR);
            editor.accept(note.getFilePath());
        }
        return note;
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

    @Override
    public String modeDisplay() {
        clearScreen();
        return TableConfig.TEXT_DASHBOARD_FORMATTER.apply(this) + "\n";

    }

    @Override
    public String eval(String input) {
        InputPrompt<TextEntry> textPrompt = new InputPrompt<>(manager.getEntries());

        String cmds = String.join("\n", "\nAvailable Actions:",
                TableUtil.basicRow(2, "new", "update <index>", "update <name>", "delete <index>", "delete <name>"),
                TableUtil.basicRow(2, "open <index>", "open <name>", "view <index>", "view <name>"),
                TableUtil.basicRow(2, "filter<all>, filter <tag>", "filter <name>, filter created"),
                TableUtil.basicRow(2, "search <String>", "archive <index>, archive <name>", "done"));

        String output = "";
        while (true) {
            try {
                clearAndPrint(TableUtil.generateTableWithHeader(
                        "Manage Projects",
                        textPrompt.getFiltered(),
                        TableConfig.TEXT_MANAGE_TABLE)
                );
                printLnToTerminal(cmds);

                if (!output.isEmpty()) {
                    printLnToTerminal("\n" + output + "\n");
                    output = "";
                }

                String[] userInput = promptInput("Action: ").trim().split(" ");
                switch (userInput[0]) {
                    case String s when s.startsWith("done") -> {
                        clearScreen();
                        return "";
                    }

                    case String s when s.startsWith("new") -> createNewEntry();

                    case String s when s.startsWith("update") -> {
                        output = Util.isInt(userInput[1])
                                 ? textPrompt.create()
                                         .validateInputLength(userInput, 2)
                                         .validateAndGetIndex(userInput[1])
                                         .itemConsumer(this::updateEntry)
                                         .display(item -> "Updated: " + item.name())
                                 : textPrompt.create()
                                         .validateInputLength(userInput, 2)
                                         .forceSelect(Util.entryMatch(textPrompt.getItems(), String.join(" ", userInput)))
                                         .itemConsumer(this::updateEntry)
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
                                         .forceSelect(Util.entryMatch(textPrompt.getItems(), String.join(" ", userInput)))
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
                                         .itemConsumer(i -> Settings.getEditor(Settings.FULL_NOTE_EDITOR).accept(i.getFilePath()))
                                         .display(__ -> "")
                                 : textPrompt.create()
                                         .validateInputLength(userInput, 2)
                                         .forceSelect(Util.entryMatch(textPrompt.getItems(), String.join(" ", userInput)))
                                         .itemConsumer(i -> Settings.getEditor(Settings.FULL_NOTE_EDITOR).accept(i.getFilePath()))
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
                                         .forceSelect(Util.entryMatch(textPrompt.getItems(), String.join(" ", userInput)))
                                         .itemConsumer(this::viewEntry)
                                         .display(__ -> "");
                    }

                    case String s when s.startsWith("filter") -> filterPrompt(userInput, textPrompt);

                    case String s when s.startsWith("search") && userInput.length > 1 -> {
                        List<SearchResult> results = manager.searchEntries(
                                String.join("\n", Arrays.copyOfRange(userInput, 1, userInput.length))
                        );
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
                                         .forceSelect(Util.entryMatch(textPrompt.getItems(), String.join(" ", userInput)))
                                         .itemConsumer(manager::archiveEntry)
                                         .listRemove()
                                         .display(i -> "Archived: " + i.name());
                    }

                    default -> output = "Invalid Input";
                }
            } catch (IOException e) {
                //do something
            }
        }
    }

    public void viewEntry(TextEntry entry) {
        clearAndPrint(TableUtil.generateTable(List.of(entry.description()), TableConfig.TEXT_VIEW_TABLE));
        promptDate("Press Enter To Return To Menu");
    }

    public void viewSearchResults(List<SearchResult> results) {

    }

}
