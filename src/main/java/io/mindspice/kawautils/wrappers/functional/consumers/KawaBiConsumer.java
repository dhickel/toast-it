package io.mindspice.kawautils.wrappers.functional.consumers;

import gnu.mapping.Procedure;

import java.util.function.BiConsumer;


public record KawaBiConsumer<T, U>(
        Procedure consumerProcedure
) implements BiConsumer<T , U> {

    public static <T, U> KawaBiConsumer<T, U> of(Procedure procedure) {
        return new KawaBiConsumer<>(procedure);
    }

    @Override
    public void accept(T t, U u) {
        try {
            consumerProcedure.apply2(t, u);
        } catch (Throwable e) {
            throw new IllegalStateException("Failed to apply procedure: " + e.getMessage());
        }
    }
}
