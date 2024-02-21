package io.mindspice.kawautils.wrappers.functional.predicates;

import gnu.mapping.Procedure;

import java.util.function.BiPredicate;


public record KawaBiPredicate<T, U>(
        Procedure predicateProcedure
) implements BiPredicate<T, U> {

    public static <T, U> KawaBiPredicate<T, U> of(Procedure procedure) {
        return new KawaBiPredicate<>(procedure);
    }

    @Override
    public boolean test(T t, U u ) {
        try {
            return (boolean) predicateProcedure.apply2(t, u);
        } catch (Throwable e) {
            throw new IllegalStateException("Failed to apply procedure: " + e.getMessage());
        }
    }
}
