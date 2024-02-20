package io.mindspice.toastit.util;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;


public record Editor(
        List<String> args
) implements Consumer<Path> {

    public static Editor of(String... args) {
        return new Editor(Arrays.stream(args).toList());
    }

    @Override
    public void accept(Path path) {
        var fullArgs = new ArrayList<>(args);
        fullArgs.add(path.toAbsolutePath().toString());
        try {
            var proc = new ProcessBuilder(fullArgs).start();;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}