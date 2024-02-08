package application;

import entries.task.FullTaskEntry;
import util.JSON;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;


public class Database {
    private static Database INSTANCE = new Database();


    public static Database instance(){
        return INSTANCE;
    }

    public FullTaskEntry getTask(UUID uuid) throws IOException {
        // TODO lookup the uuid and load from the file;
        Path path = Path.of("sd");
        return JSON.loadObjectFromFile(path, FullTaskEntry.class);
    }
}
