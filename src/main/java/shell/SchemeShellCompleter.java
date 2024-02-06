package shell;

import kawa.standard.Scheme;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;


public class SchemeShellCompleter implements Completer {
    ShellMode mode = ShellMode.COMMAND;

    private List<Candidate> console = Stream.of(
                    "command", "scheme", "exit", "help", "clear", "system")
            .map(Candidate::new).toList();

    private List<Candidate> browser = Stream.of(
                    "ls", "nano", "mkdir", "cp", "touch", "cd", "mv", "rm", "rm-confirm", "rm-abort", "rm-path")
            .map(Candidate::new).toList();

    private List<Candidate> scheme = new ArrayList<>(2000);

    public void setMode(ShellMode mode) { this.mode = mode; }

    public String loadSchemeCompletions(Scheme schemeInstance) {
        int prevSize = scheme.size();
        scheme.clear();
        schemeInstance.getEnvironment().enumerateAllLocations().forEachRemaining(e ->
                scheme.add(new Candidate("(" + e.getKeySymbol().toString()))
        );
        schemeInstance.getEnvironment().enumerateAllLocations().forEachRemaining(e ->
                scheme.add(new Candidate(e.getKeySymbol().toString()))
        );
        return "Loaded " + scheme.size() + "symbols | " + (scheme.size() - prevSize) + " new";
    }

    @Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        switch (mode) {
            case COMMAND -> candidates.addAll(console);
            case SYSTEM -> candidates.addAll(browser);
            case SCHEME -> candidates.addAll(scheme);
        }

    }
}
