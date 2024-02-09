package shell;

import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;

import java.util.ArrayList;
import java.util.List;


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
                    e.printStackTrace();
                    return "Exception encountered while executing command: " + e.getMessage();
                }
            }
        }
        return "Invalid command or input.";
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
}
