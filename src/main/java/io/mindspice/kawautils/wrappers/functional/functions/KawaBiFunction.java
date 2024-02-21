package io.mindspice.kawautils.wrappers.functional.functions;

import gnu.mapping.Procedure;

import java.util.function.BiFunction;


public record KawaBiFunction<T, U, R>(
        Procedure functionProcedure
) implements BiFunction<T, U, R> {

    public static <T, U, R> KawaBiFunction<T, U, R> of(Procedure procedure) {
        return new KawaBiFunction<>(procedure);
    }

    @Override
    public R apply(T t, U u) {
        try {
            @SuppressWarnings("unchecked")
            R result = (R) functionProcedure.apply2(t, u);
            return result;
        } catch (Throwable e) {
            throw new IllegalStateException("Failed to apply procedure: " + e.getMessage());
        }
    }
}
