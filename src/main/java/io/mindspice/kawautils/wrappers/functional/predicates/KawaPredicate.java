package io.mindspice.kawautils.wrappers.functional.predicates;

import gnu.mapping.Procedure;

import java.util.function.Predicate;


public record KawaPredicate<T>(
        Procedure predicateProcedure
) implements Predicate<T> {

    public static <T> KawaPredicate<T> of(Procedure procedure) {
        return new KawaPredicate<>(procedure);
    }

    @Override
    public boolean test(T t) {
        try {
            return (boolean) predicateProcedure.apply1(t);
        } catch (Throwable e) {
            throw new IllegalStateException("Failed to apply procedure: " + e.getMessage());
        }
    }
}
