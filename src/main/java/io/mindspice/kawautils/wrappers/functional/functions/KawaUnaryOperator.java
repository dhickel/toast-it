package io.mindspice.kawautils.wrappers.functional.functions;

import gnu.mapping.Procedure;

import java.util.Arrays;
import java.util.function.UnaryOperator;


public record KawaUnaryOperator<T>(
        Procedure functionProcedure
) implements UnaryOperator<T> {

    public static <T> KawaUnaryOperator<T> of(Procedure procedure) {
        return new KawaUnaryOperator<>(procedure);
    }

    @Override
    public T apply(T t) {
        try {
            @SuppressWarnings("unchecked")
            T result = (T) functionProcedure.apply1(t);
            return result;
        } catch (Throwable e) {
            throw new IllegalStateException("Failed to apply procedure: " + Arrays.toString(e.getStackTrace()));
        }
    }
}
