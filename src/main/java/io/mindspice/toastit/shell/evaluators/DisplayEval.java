package io.mindspice.toastit.shell.evaluators;

import java.util.function.Supplier;


public class DisplayEval extends ShellEvaluator<DisplayEval> {
    public Supplier<String> displaySupplier;

    public DisplayEval(Supplier<String> displaySupplier) {
        this.displaySupplier = displaySupplier;
    }

    @Override
    public String modeDisplay() {
        return displaySupplier.get();
    }
}
