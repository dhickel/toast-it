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

    public String eval(String[] input) {
        for (var cmd : commands) {
            if (cmd.match(input)) {
                try {
                    @SuppressWarnings("unchecked")
                    T self = (T) this;
                    return cmd.eval(self, input, lineReader, terminal);
                } catch (Exception e) {
                    return "Exception encountered while executing command: " + e.getMessage();
                }
            }
        }
        return "Invalid command or input.";
    }
}
