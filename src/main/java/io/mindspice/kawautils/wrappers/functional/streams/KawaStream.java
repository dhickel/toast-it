package io.mindspice.kawautils.wrappers.functional.streams;

import gnu.lists.LList;
import gnu.lists.Pair;

import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


public class KawaStream {

    public static <T> Stream<T> toStream(Object rawList, Function<Object, T> converter) {
        if (rawList instanceof LList list) {
            return StreamSupport.stream(
                    Spliterators.spliteratorUnknownSize(new LListIterator<>(list, converter), Spliterator.ORDERED), false);
        } else {
            throw new ClassCastException("Object is not an instance of LList");
        }
    }

    private static class LListIterator<T> implements java.util.Iterator<T> {
        private Object current;
        private final Function<Object, T> converter;

        public LListIterator(LList list, Function<Object, T> converter) {
            this.current = list;
            this.converter = converter;
        }

        @Override
        public boolean hasNext() {
            return current instanceof Pair;
        }

        @Override
        public T next() {
            if (!(current instanceof Pair pair)) {
                throw new NoSuchElementException();
            }

            T convertedValue = converter.apply(pair.getCar());
            current = pair.getCdr();

            return convertedValue;
        }
    }
}
