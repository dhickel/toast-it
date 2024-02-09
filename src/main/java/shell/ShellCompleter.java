package shell;

import kawa.standard.Scheme;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;


public class ShellCompleter implements Completer {
    String mode;
    HashMap<String, List<Candidate>> completionMap = new HashMap<>(4);

    private List<Candidate> browser = Stream.of(
                    "ls", "nano", "mkdir", "cp", "touch", "cd", "mv", "rm", "rm", "rm", "rm")
            .map(Candidate::new).toList();

    private List<Candidate> scheme = new ArrayList<>(2000);

    public void setMode(String mode) { this.mode = mode; }

    public String loadSchemeCompletions(Scheme schemeInstance) {
        int prevSize = scheme.size();
        scheme.clear();
        schemeInstance.getEnvironment().enumerateAllLocations().forEachRemaining(e ->
                scheme.add(new Candidate("(" + e.getKeySymbol().toString()))
        );
        schemeInstance.getEnvironment().enumerateAllLocations().forEachRemaining(e ->
                scheme.add(new Candidate(e.getKeySymbol().toString()))
        );
        completionMap.put("SCHEME", scheme);
        completionMap.put("DIRECTORY", browser);
        return "Loaded " + scheme.size() + "symbols | " + (scheme.size() - prevSize) + " new";
    }

    @Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        candidates.addAll(completionMap.get(mode));
    }
}
