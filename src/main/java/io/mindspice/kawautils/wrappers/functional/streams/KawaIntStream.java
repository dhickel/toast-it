package io.mindspice.kawautils.wrappers.functional.streams;

import gnu.lists.LList;
import gnu.lists.Pair;
import gnu.math.IntNum;

import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;


public class KawaIntStream {

    public static IntStream toStream(Object rawList) {
        if (rawList instanceof LList list) {
            return StreamSupport.intStream(
                    Spliterators.spliteratorUnknownSize(new IntNumIterator(list), Spliterator.ORDERED), false);
        } else {
            throw new ClassCastException("Object is not an instance of LList");
        }
    }

    private static class IntNumIterator implements java.util.PrimitiveIterator.OfInt {
        private Object current;

        public IntNumIterator(LList list) {
            this.current = list;
        }

        @Override
        public boolean hasNext() {
            return current instanceof Pair;
        }

        @Override
        public int nextInt() {
            if (!(current instanceof Pair pair)) {
                throw new NoSuchElementException();
            }

            Object value = pair.getCar();
            current = pair.getCdr();

            if (value instanceof IntNum intNum) {
                return intNum.intValue();
            }

            throw new NoSuchElementException("Element is not an IntNum");
        }
    }
}
