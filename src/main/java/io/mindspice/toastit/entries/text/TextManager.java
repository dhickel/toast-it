package io.mindspice.toastit.entries.text;

import io.mindspice.toastit.App;
import io.mindspice.toastit.entries.SearchResult;
import io.mindspice.toastit.enums.EntryType;
import io.mindspice.toastit.util.DateTimeUtil;
import io.mindspice.toastit.util.Settings;

import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;


public class TextManager {

    public final List<TextEntry> entries = new CopyOnWriteArrayList<>();
    public EntryType type;
    public volatile TextEntry dailyJournal;

    public void init(EntryType type) throws IOException {
        if (type == EntryType.NOTE) {
            entries.addAll(App.instance().getDatabase().getAllNotes().stream().map(n -> n.getAsFull(EntryType.NOTE)).toList());
        } else if (type == EntryType.JOURNAL) {
            entries.addAll(App.instance().getDatabase().getAllJournals().stream().map(j -> j.getAsFull(EntryType.JOURNAL)).toList());
        } else {
            throw new IllegalStateException("Invalid Entry Type");
        }
        this.type = type;
    }

    public List<TextEntry> getEntries() {
        return entries;
    }

    public void addEntry(TextEntry entry) throws IOException {
        if (type == EntryType.NOTE) {
            addNote(entry);
        } else {
            addJournal(entry);
        }
    }

    private void addNote(TextEntry note) throws IOException {
        App.instance().getDatabase().upsertNote(note);
        entries.add(note);
        note.flushToDisk();
    }

    private void addJournal(TextEntry journal) throws IOException {
        App.instance().getDatabase().upsertJournal(journal);
        entries.add(journal);
        journal.flushToDisk();
    }

    public void updateEntry(TextEntry entry) {
        if (type == EntryType.NOTE) {
            updateNote(entry);
        } else {
            updateJournal(entry);
        }
    }

    public TextEntry getDailyJournal() throws IOException {
        if (dailyJournal == null || dailyJournal.createdAt().truncatedTo(ChronoUnit.DAYS)
                .isBefore(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS))) {
            var builder = TextEntry.builder(EntryType.JOURNAL);
            builder.createdAt = LocalDateTime.now();
            builder.name = "Daily Journal | " + DateTimeUtil.printDateTimeFull(LocalDateTime.now());
            builder.tags = List.of("daily");
            dailyJournal = builder.build();
            addJournal(dailyJournal);
        }
        return dailyJournal;
    }

    private void updateNote(TextEntry note) {
        try {
            App.instance().getDatabase().upsertNote(note);
            entries.removeIf(n -> n.uuid() == note.uuid());
            entries.add(note);
            note.flushToDisk();
        } catch (IOException e) {
            System.err.println("Error updating note: " + note.uuid() + " | " + Arrays.toString(e.getStackTrace()));
        }
    }

    private void updateJournal(TextEntry journal) {
        try {
            App.instance().getDatabase().upsertJournal(journal);
            entries.removeIf(j -> j.uuid() == journal.uuid());
            entries.add(journal);
            journal.flushToDisk();
        } catch (IOException e) {
            System.err.println("Error updating journal: " + journal.uuid() + " | " + Arrays.toString(e.getStackTrace()));
        }
    }

    public void deleteEntry(TextEntry entry) {
        if (type == EntryType.NOTE) {
            deleteNote(entry);
        } else {
            deleteJournal(entry);
        }
    }

    private void deleteNote(TextEntry note) {
        try {
            entries.removeIf(n -> n.uuid() == note.uuid());
            Files.delete(note.getFilePath());
        } catch (IOException e) {
            System.err.println("Error deleting note: " + note.uuid() + " | " + Arrays.toString(e.getStackTrace()));
        }
    }

    private void deleteJournal(TextEntry journal) {
        try {
            entries.removeIf(n -> n.uuid() == journal.uuid());
            Files.delete(journal.getFilePath());
        } catch (IOException e) {
            System.err.println("Error deleting Journal: " + journal.uuid() + " | " + Arrays.toString(e.getStackTrace()));
        }
    }

    public void archiveEntry(TextEntry entry) {
        if (type == EntryType.NOTE) {
            archiveNote(entry);
        } else {
            archiveJournal(entry);
        }
    }

    private void archiveNote(TextEntry note) {
        try {
            entries.removeIf(n -> n.uuid() == note.uuid());
            App.instance().getDatabase().archiveNote(note.uuid(), true);
        } catch (IOException e) {
            System.err.println("Error deleting note: " + note.uuid() + " | " + Arrays.toString(e.getStackTrace()));
        }
    }

    private void archiveJournal(TextEntry journal) {
        try {
            entries.removeIf(n -> n.uuid() == journal.uuid());
            App.instance().getDatabase().archiveJournal(journal.uuid(), true);
        } catch (IOException e) {
            System.err.println("Error deleting Journal: " + journal.uuid() + " | " + Arrays.toString(e.getStackTrace()));
        }
    }

    public List<SearchResult> searchEntries(String searchString) {
        return searchForEntries(searchString, entries);
    }

    private List<SearchResult> searchForEntries(String searchString, List<TextEntry> searchList) {
        if (Settings.THREADED_SEARCH && searchList.size() > 10) {
            try (var exec = Executors.newVirtualThreadPerTaskExecutor()) {
                return searchList.stream()
                        .map(entry -> exec.submit(() -> searchFiles(searchString, entry))).toList()
                        .parallelStream().map(future -> {
                            try {
                                return future.get(Settings.SEARCH_TIMEOUT_SEC, TimeUnit.SECONDS);
                            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                                Thread.currentThread().interrupt();
                                System.err.println("Error while searching: " + e);
                                return new ArrayList<SearchResult>(0);
                            }
                        }).flatMap(List::stream).toList();
            }
        } else {
            return searchList.stream().map(entry -> searchFiles(searchString, entry)).flatMap(List::stream).toList();
        }
    }

    public List<SearchResult> searchFiles(String searchString, TextEntry entry) {
        List<SearchResult> results = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(entry.getFilePath());
            lines.forEach(line -> {
                if (line.contains(searchString)) {
                    results.add(new SearchResult(line, entry));
                }
            });
        } catch (IOException e) {
            System.err.println("Error reading file: " + entry.getFilePath());
        }
        return results;
    }


}

