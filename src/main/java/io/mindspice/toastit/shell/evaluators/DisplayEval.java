package io.mindspice.toastit.shell.evaluators;

import io.mindspice.toastit.shell.ShellCommand;

import java.util.function.Supplier;


public class DisplayEval extends ShellEvaluator<DisplayEval> {
    public Supplier<String> displaySupplier;

    public DisplayEval(Supplier<String> displaySupplier) {
        this.displaySupplier = displaySupplier;
        commands.add(ShellCommand.of("refresh", (__, ___) -> modeDisplay()));
    }

    @Override
    public String modeDisplay() {
        return displaySupplier.get();
    }
}
