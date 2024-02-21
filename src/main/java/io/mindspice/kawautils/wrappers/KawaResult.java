package io.mindspice.kawautils.wrappers;

import java.util.Optional;


public record KawaResult<T>(
        Optional<T> result,
        Optional<Throwable> exception
) {
    public boolean valid() {
        return exception.isEmpty();
    }

    public boolean validAndPresent() {
        return exception.isEmpty() || result.isPresent();
    }

    public T get() {
        return result.get();
    }

    public T getOrNull() {
        return result.orElse(null);
    }
}
