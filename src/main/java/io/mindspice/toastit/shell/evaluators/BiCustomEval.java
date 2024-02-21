package io.mindspice.toastit.shell.evaluators;

import java.util.function.Supplier;


public class BiCustomEval<T, U> extends ShellEvaluator<BiCustomEval<T, U>> {
    public T manager1;
    public U manager2;
    public Supplier<String> modeSupplier;

    public BiCustomEval(T manager1, U manager2, Supplier<String> modeSupplier) {
        this.manager1 = manager1;
        this.manager2 = manager2;
        this.modeSupplier = modeSupplier;
    }

    @Override
    public String modeDisplay() {
        return modeSupplier.get();
    }
}
