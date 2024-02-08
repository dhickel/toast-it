package shell;

import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;

import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;


public record ShellCommand<T>(
        Predicate<String[]> predicate,
        BiFunction<T, String[], String> command

) implements CommandMatcher<T, String[], LineReader, Terminal, String> {

    public static <T> ShellCommand<T> of(String cmd, BiFunction<T, String[], String> cmdFunction) {
        return new ShellCommand<>((input) -> input[0].toLowerCase().equals(cmd), cmdFunction);
    }

    public static <T> ShellCommand<T> ofAliased(Set<String> cmdAndAliases, BiFunction<T, String[], String> cmdFunction) {
        return new ShellCommand<>((input) -> cmdAndAliases.contains(input[0].toLowerCase()), cmdFunction);
    }

    @Override
    public boolean match(String[] strings) {
        return predicate.test(strings);
    }

    @Override
    public String eval(T t, String[] strings, LineReader lineReader, Terminal terminal) {
        return command.apply(t, strings);
    }
}
