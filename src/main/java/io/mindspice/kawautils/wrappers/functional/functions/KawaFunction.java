package io.mindspice.kawautils.wrappers.functional.functions;

import gnu.mapping.Procedure;

import java.util.Arrays;
import java.util.function.Function;


public record KawaFunction<T, R>(
        Procedure functionProcedure
) implements Function<T, R> {

    public static <T, R> KawaFunction<T, R> of(Procedure procedure) {
        return new KawaFunction<>(procedure);
    }

    @Override
    public R apply(T t) {
        try {
            @SuppressWarnings("unchecked")
            R result = (R) functionProcedure.apply1(t);
            return result;
        } catch (Throwable e) {
            throw new IllegalStateException("Failed to apply procedure: " + Arrays.toString(e.getStackTrace()));
        }
    }
}
