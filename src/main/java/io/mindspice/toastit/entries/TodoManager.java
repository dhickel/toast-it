package io.mindspice.toastit.entries;

import io.mindspice.toastit.util.JSON;
import io.mindspice.toastit.util.Settings;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class TodoManager {
    public final Path todoFile;
    public List<String> todoItems = new CopyOnWriteArrayList<>();

    public TodoManager() throws IOException {
       todoFile = Path.of(Settings.ROOT_PATH, "items.todo");
        if (!Files.exists(todoFile)) {
            Files.createFile(todoFile);
            flushToDisk();
        }
        List<String> items = JSON.jsonArrayToStringList(Files.readString(todoFile));
        todoItems.addAll(items);
    }

    public void flushToDisk() throws IOException {
        Files.writeString(todoFile, JSON.writePretty(todoItems));
    }

    public void addItem(String item) {
        try {
            todoItems.add(item);
            flushToDisk();
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    public void removeItem(String item) {
        try {
            todoItems.remove(item);
            flushToDisk();
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    public List<String> getAllItems() {
        return todoItems;
    }


}
