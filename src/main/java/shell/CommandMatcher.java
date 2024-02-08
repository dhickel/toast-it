package shell;

public interface CommandMatcher<T, U, V, W, R> {
    boolean match(U u);

    R eval(T t, U u, V v, W w);
}
