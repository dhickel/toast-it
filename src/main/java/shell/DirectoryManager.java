package shell;

import org.jline.builtins.Nano;
import org.jline.terminal.Terminal;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.time.Instant;


public class DirectoryManager {
    private Path currPath;
    private Path rmPath;
    private Terminal terminal;

    public DirectoryManager(Terminal terminal) {
        this.currPath = Path.of(System.getProperty("user.dir")).toAbsolutePath();
        this.terminal = terminal;
    }

    public Path getCurrPath() {
        return currPath;
    }

    public String processCommand(String input) {
        if (input.startsWith("system")) {
            input = input.replace("system", "");
        }
        input = input.trim();
        if (input.isEmpty()) { return ""; }
        String[] dirCommand = input.split(" ");

        switch (dirCommand[0]) {
            case String cmd when cmd.equalsIgnoreCase("ls") -> { return listDir(currPath); }
            case String cmd when cmd.toLowerCase().startsWith("nano") -> { return nano(input); }
            case String cmd when cmd.toLowerCase().startsWith("mkdir") -> { return mkdir(input); }
            case String cmd when cmd.toLowerCase().startsWith("cp") -> { return copy(input); }
            case String cmd when cmd.toLowerCase().startsWith("touch") -> { return touch(input); }
            case String cmd when cmd.equalsIgnoreCase("cd") -> { return changeDir(dirCommand); }
            case String cmd when cmd.toLowerCase().startsWith("mv") -> { return move(input); }
            case String cmd when cmd.toLowerCase().startsWith("rm") -> {
                if (cmd.toLowerCase().contains("-confirm")) { return removeConfirm(); }
                if (cmd.toLowerCase().contains("-abort")) { return removeAbort(); }
                if (cmd.toLowerCase().contains("-path")) { return rmPath == null ? "Error: null path" : rmPath.toString(); }
                return remove(input);
            }
            default -> { return "Error: Invalid directory or command"; }
        }
    }

    private String nano(String input) {
        String[] cmdParts = input.split(" ", 2);
        if (cmdParts.length < 2) {
            return "Error: No file path specified for nano";
        }
        String filePath = cmdParts[1];

        Path file = currPath.resolve(filePath).normalize();
        if (!file.toFile().exists() || file.toFile().isDirectory()) {
            return "Invalid file path: " + file;
        }

        launchNano(file.toFile(), terminal);
        return "Exited nano";
    }

    private String mkdir(String input) {
        String[] cmdParts = input.split(" ", 2);
        if (cmdParts.length < 2) {
            return "Error: No directory name specified for mkdir";
        }
        Path newDir = currPath.resolve(cmdParts[1]).normalize();
        if (!Files.exists(newDir)) {
            try {
                Files.createDirectories(newDir);
            } catch (IOException e) {
                return "Error: Error creating directory | " + e.getMessage();
            }
            return "Directory created: " + newDir;
        } else {
            return "Directory already exists: " + newDir;
        }
    }

    private String copy(String input) {
        String[] cmdParts = input.split(" ");
        if (cmdParts.length < 3) {
            return "Error: cp requires source and destination paths";
        }
        Path source = currPath.resolve(cmdParts[1]).normalize();
        Path destination = currPath.resolve(cmdParts[2]).normalize();
        if (Files.exists(source)) {
            try {
                Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                return "Error: Error copying file | " + e.getMessage();

            }
            return "File copied from " + source + " to " + destination;
        } else {
            return "Source file does not exist: " + source;
        }
    }

    private String touch(String input) {
        String[] cmdParts = input.split(" ", 2);
        if (cmdParts.length < 2) {
            return "Error: No file name specified for touch";
        }
        Path file = currPath.resolve(cmdParts[1]).normalize();
        try {
            if (!Files.exists(file)) {
                Files.createFile(file);
                return "File created: " + file;
            } else {
                Files.setLastModifiedTime(file, FileTime.from(Instant.now()));
                return "Updated file timestamp: " + file;
            }
        } catch (IOException e) {
            return "Error: Error running touch command | " + e.getMessage();
        }
    }

    private String changeDir(String[] dirCommand) {
        String path = "";
        if (dirCommand.length < 2) { return "Error: No path specified"; }
        String newPath = dirCommand[1];
        if (newPath.startsWith("/")) {// Absolute path
            Path resolvedPath = Paths.get(newPath).normalize();
            path = resolvedPath.toString();
            if (resolvedPath.toFile().exists() && resolvedPath.toFile().isDirectory()) {
                currPath = resolvedPath;
            } else {
                return "Invalid path: " + resolvedPath;
            }
        } else {// Relative path
            String[] pathParts = newPath.split("/");
            for (String part : pathParts) {
                if ("..".equals(part)) { // Move up
                    if (currPath.getParent() != null) { currPath = currPath.getParent(); }
                } else { // Move into
                    Path resolvedPath = currPath.resolve(part).normalize();
                    path = resolvedPath.toString();
                    if (resolvedPath.toFile().exists() && resolvedPath.toFile().isDirectory()) {
                        currPath = resolvedPath;
                    } else {
                        return "Invalid path: " + resolvedPath;
                    }
                }
            }
        }
        return "Current Path: " + path;
    }

    public String move(String input) {
        String[] cmdParts = input.split(" ");
        if (cmdParts.length < 3) {
            return "Error: mv requires source and destination paths";
        }
        Path sourcePath = currPath.resolve(cmdParts[1]).normalize();
        Path destinationPath = currPath.resolve(cmdParts[2]).normalize();

        if (Files.exists(sourcePath)) {
            try {
                Files.move(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                return "Error: Error running mv command | " + e.getMessage();
            }
            return "Moved from " + sourcePath + " to " + destinationPath;
        } else {
            return "Source file or directory does not exist: " + sourcePath;
        }
    }

    public String remove(String input) {
        String[] cmdParts = input.split(" ", 2);
        if (cmdParts.length < 2) {
            return "Error: No file or directory specified for rm";
        }
        Path pathToRemove = currPath.resolve(cmdParts[1]).normalize();

        if (Files.exists(pathToRemove)) {
            if (Files.isDirectory(pathToRemove)) {
                return "Error: Removing directory unsupported: " + pathToRemove;
            }
            rmPath = pathToRemove;
            return "Set removal to: " + pathToRemove + "\n"
                    + "Enter rm-confirm to delete, rm-abort to abort deletion, or rm-path to display queued deletion";
        }
        return "Error: File does not exist";
    }

    public String removeConfirm() {
        try {
            Files.delete(rmPath);
        } catch (IOException e) {
            return "Error: Error running rm command | " + e.getMessage();
        }
        return "Removed: " + rmPath;
    }

    public String removeAbort() {
        String oldPath = rmPath.toString();
        rmPath = null;
        return "Aborted removal of: " + oldPath;
    }

    private String listDir(Path path) {
        File dir = path.toFile();
        if (!dir.exists() || !dir.isDirectory()) {
            return "Invalid directory path.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("\"").append(path).append("\"\n");
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                sb.append(file.getName()).append("\n");
            }
        }
        return sb.toString();
    }

    public void launchNano(File file, Terminal terminal) {
        try {
            if (!file.canRead() || (!file.canWrite() && file.exists())) {
                System.out.println("File cannot be read or written");
                return;
            }
            Nano nano = new Nano(terminal, file);

            nano.tabs = 2;
            nano.matchBrackets = "(<[{)>]}";
            nano.brackets = "\"’)>]}";
            nano.mouseSupport = true;
            nano.open(file.getAbsolutePath());
            nano.run();
            nano.setRestricted(true);

        } catch (Exception e) {
            System.out.println("Error launching nano: " + e.getMessage());
        }
    }
}
