package shell;

import java.util.Set;
import java.util.function.Predicate;


public record ShellMode<T>(
    String mode,
    Set<String> aliases,
    ShellEvaluator<T> modeInstance
) implements Predicate<String> {

    @Override
    public boolean test(String s) {
        return aliases.contains(s);
    }
}
