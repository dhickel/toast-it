package shell;

import java.util.Set;
import java.util.function.Predicate;


public record ShellMode<T>(
        String mode,
        Set<String> aliases,
        ShellEvaluator<T> modeInstance,
        String promptSymbol,
        String altPromptSymbol
) implements Predicate<String> {
    @Override
    public boolean test(String s) {
        return aliases.stream().anyMatch(s::startsWith);
    }

}
