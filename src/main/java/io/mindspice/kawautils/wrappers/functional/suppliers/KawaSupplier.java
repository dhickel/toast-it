package io.mindspice.kawautils.wrappers.functional.suppliers;

import gnu.mapping.Procedure;

import java.util.function.Supplier;


public record KawaSupplier<T>(
        Procedure supplierProcedure
) implements Supplier<T> {

    public static <T> KawaSupplier<T> of(Procedure procedure) {
        return new KawaSupplier<>(procedure);
    }

    @Override
    public T get() {
        try {
            @SuppressWarnings("unchecked")
            T result = (T) supplierProcedure.apply0();
            return result;
        } catch (Throwable e) {
            throw new IllegalStateException("Failed to apply procedure: " + e.getMessage());
        }
    }
}
