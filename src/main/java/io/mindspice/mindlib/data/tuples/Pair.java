package io.mindspice.mindlib.data.tuples;

import java.util.Map;


public record Pair<U, V>(U first, V second) {

    public static <U, V> Pair<U, V> of(U obj1, V obj2) {
        return new Pair<>(obj1, obj2);
    }

    public static <U, V> Pair<U, V> of(Map.Entry<U, V> entry) {
        return new Pair<>(entry.getKey(), entry.getValue());
    }
}
