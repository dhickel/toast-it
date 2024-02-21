package io.mindspice.kawautils.wrappers;

import gnu.mapping.Procedure;

import java.util.Arrays;


public record KawaRunnable(
        Procedure runnableProcedure
) implements Runnable {

    public static KawaRunnable of(Procedure procedure) {
        return new KawaRunnable(procedure);
    }

    @Override
    public void run() {
        try {
            runnableProcedure.apply0();
        } catch (Throwable e) {
            throw new IllegalStateException("Failed to run procedure: " + Arrays.toString(e.getStackTrace()));
        }
    }
}
