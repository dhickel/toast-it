package io.mindspice.kawautils.wrappers.functional.consumers;

import gnu.mapping.Procedure;

import java.util.function.Consumer;


public record KawaConsumer<T>(
        Procedure consumerProcedure
) implements Consumer<T> {

    public static <T> Consumer<T> of(Procedure procedure) {

        return new KawaConsumer<>(procedure);
    }

    @Override
    public void accept(T t) {
        try {
            consumerProcedure.apply1(t);
        } catch (Throwable e) {
            throw new IllegalStateException("Failed to apply procedure: " + e.getMessage());
        }
    }
}
