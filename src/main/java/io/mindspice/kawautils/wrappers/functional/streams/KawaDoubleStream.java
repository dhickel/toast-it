package io.mindspice.kawautils.wrappers.functional.streams;

import gnu.lists.LList;
import gnu.lists.Pair;
import gnu.math.RealNum;

import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.DoubleStream;
import java.util.stream.StreamSupport;


public class KawaDoubleStream {

    public static DoubleStream toStream(Object rawList) {
        if (rawList instanceof LList list) {
            return StreamSupport.doubleStream(
                    Spliterators.spliteratorUnknownSize(new DoubleNumIterator(list), Spliterator.ORDERED), false);
        } else {
            throw new ClassCastException("Object is not an instance of LList");
        }
    }

    private static class DoubleNumIterator implements PrimitiveIterator.OfDouble {
        private Object current;

        public DoubleNumIterator(LList list) {
            this.current = list;
        }

        @Override
        public boolean hasNext() {
            return current instanceof Pair;
        }

        @Override
        public double nextDouble() {
            if (!(current instanceof Pair pair)) {
                throw new NoSuchElementException();
            }

            Object value = pair.getCar();
            current = pair.getCdr();

            if (value instanceof RealNum intNum) {
                return intNum.doubleValue();
            }

            throw new NoSuchElementException("Element is not an IntNum");
        }
    }
}
