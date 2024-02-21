package io.mindspice.toastit.shell.evaluators;

import java.util.function.Supplier;


public class CustomEval<T> extends ShellEvaluator<CustomEval<T>> {
    public T manager;
    public Supplier<String> modeSupplier;

    public CustomEval(T manager, Supplier<String> modeSupplier) {
        this.manager = manager;
        this.modeSupplier = modeSupplier;
    }

    @Override
    public String modeDisplay() {
        return modeSupplier.get();
    }
}
