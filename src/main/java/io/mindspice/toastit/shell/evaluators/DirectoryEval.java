package io.mindspice.toastit.shell.evaluators;

import org.jline.builtins.Nano;
import org.jline.terminal.Terminal;
import io.mindspice.toastit.shell.ShellCommand;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.List;


public class DirectoryEval extends ShellEvaluator<DirectoryEval> {
    private Path currPath = Path.of(System.getProperty("user.dir")).toAbsolutePath();


    @Override
    public String modeDisplay() {
        return "Entered Directory Mode";
    }
    public DirectoryEval() {
        initBaseCommands();
    }

    public DirectoryEval(List<ShellCommand<DirectoryEval>> userCommands) {
        commands.addAll(userCommands);
        initBaseCommands();

    }

    public void initBaseCommands() {
        List<ShellCommand<DirectoryEval>> commandInit = List.of(
                ShellCommand.of("ls", DirectoryEval::listDir),
                ShellCommand.of("nano", DirectoryEval::nano),
                ShellCommand.of("mkdir", DirectoryEval::mkdir),
                ShellCommand.of("cp", DirectoryEval::copy),
                ShellCommand.of("touch", DirectoryEval::touch),
                ShellCommand.of("cd", DirectoryEval::changeDir),
                ShellCommand.of("mv", DirectoryEval::move),
                ShellCommand.of("rm", DirectoryEval::remove)
        );
        commands.addAll(commandInit);
    }

    public Path getCurrPath() {
        return currPath;
    }

    private String nano(String input) {
        String[] splitCmd = input.split(" ");
        if (splitCmd.length < 2) {
            return "Error: No file path specified for nano";
        }
        String filePath = splitCmd[1];

        Path file = currPath.resolve(filePath).normalize();
        if (!file.toFile().exists() || file.toFile().isDirectory()) {
            return "Invalid file path: " + file;
        }

        launchNano(file.toFile(), terminal);
        return "Exited nano";
    }

    private String mkdir(String input) {
        String[] splitCmd = input.split(" ");
        if (splitCmd.length < 2) {
            return "Error: No directory name specified for mkdir";
        }
        Path newDir = currPath.resolve(splitCmd[1]).normalize();
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
        String[] splitCmd = input.split(" ");
        if (splitCmd.length < 3) {
            return "Error: cp requires source and destination paths";
        }
        Path source = currPath.resolve(splitCmd[1]).normalize();
        Path destination = currPath.resolve(splitCmd[2]).normalize();
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
        String[] splitCmd = input.split(" ");
        if (splitCmd.length < 2) {
            return "Error: No file name specified for touch";
        }
        Path file = currPath.resolve(splitCmd[1]).normalize();
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

    private String changeDir(String input) {
        String[] splitCmd = input.split(" ");
        if (splitCmd.length < 2) {
            return "Error: No path specified";
        }

        String path = "";
        String newPath = splitCmd[1];

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
        String[] splitCmd = input.split(" ");
        if (splitCmd.length < 3) {
            return "Error: mv requires source and destination paths";
        }
        Path sourcePath = currPath.resolve(splitCmd[1]).normalize();
        Path destinationPath = currPath.resolve(splitCmd[2]).normalize();

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
        String[] splitCmd = input.split(" ");

        if (splitCmd.length < 2) {
            return "Error: No file or directory specified for rm";
        }
        Path pathToRemove = currPath.resolve(splitCmd[1]).normalize();

        if (Files.exists(pathToRemove)) {
            if (Files.isDirectory(pathToRemove)) {
                return "Error: Removing directory unsupported: " + pathToRemove;
            }

            while (true) {
                String confirm = lineReader.readLine("Delete " + pathToRemove + "?\n Confirm (yes|no): ");
                if (confirm.equals("yes")) {
                    try {
                        Files.delete(pathToRemove);
                    } catch (IOException e) {
                        return "Error: Error running rm command | " + e.getMessage();
                    }
                    return "Removed: " + pathToRemove;
                } else if (confirm.contains("n")) {
                    break;
                }
            }
        }
        return "";
    }

    private String listDir(String input) {
        String[] splitCmd = input.split(" ");

        File dir;
        if (splitCmd.length <= 1) {
            dir = currPath.toFile();
        } else {
            dir = Path.of(splitCmd[1]).toFile();
        }

        if (!dir.exists() || !dir.isDirectory()) {
            return "Invalid directory path.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("\"").append(dir).append("\"\n");
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
                System.err.println("File cannot be read or written"); // TODO flush this to terminal
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
            System.err.println("Error launching nano: " + e.getMessage());
        }
    }


}
