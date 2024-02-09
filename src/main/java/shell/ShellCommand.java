package shell;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;


public record ShellCommand<T>(
        BiFunction<T, String, String> command,
        Set<String> aliases,
        BiPredicate<String, Set<String>> predicate
        ) {


    public static <T> ShellCommand<T> of(String cmd, BiFunction<T, String, String> cmdFunction) {
        Set<String> aliases = new HashSet<>(1);
        aliases.add(cmd);
        return new ShellCommand<>(cmdFunction,aliases, basePredicate);
    }

    public static <T> ShellCommand<T> ofAliased(Set<String> cmdAliases, BiFunction<T, String, String> cmdFunction) {
        Set<String> aliases = new HashSet<>(cmdAliases.size());
        aliases.addAll(cmdAliases);
        return new ShellCommand<>(cmdFunction,aliases, basePredicate);
    }

    public static <T> ShellCommand<T> ofMatchAny(BiFunction<T, String, String> cmdFunction) {
        return new ShellCommand<>(cmdFunction, Set.of(), (input, aliases) -> true);
    }

    public boolean match(String strings) {
        return predicate.test(strings, aliases);
    }

    public String eval(T t, String strings) {
        return command.apply(t, strings);
    }

    public void addAlias(String newAlias) {
        aliases.add(newAlias);
    }

    public boolean removeAlias(String oldAlias) {
        return aliases.remove(oldAlias);
    }

    public boolean replaceAlias(String oldAlias, String newAlias) {
        boolean found = aliases.remove(oldAlias);
        if (found) {
            aliases.add(newAlias);
        }
        return found;
    }

    private static final BiPredicate<String, Set<String>> basePredicate = (input, aliases)
            -> aliases.stream().anyMatch(input::startsWith);
}
