package io.mindspice.kawautils.wrappers.functional.streams;

import gnu.lists.LList;
import gnu.lists.Pair;
import gnu.math.IntNum;

import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.LongStream;
import java.util.stream.StreamSupport;


public class KawaLongStream {

    public static LongStream toStream(Object rawList) {
        if (rawList instanceof LList list) {
            return StreamSupport.longStream(
                    Spliterators.spliteratorUnknownSize(new LongNumIterator(list), Spliterator.ORDERED), false);
        } else {
            throw new ClassCastException("Object is not an instance of LList");
        }
    }

    private static class LongNumIterator implements PrimitiveIterator.OfLong {
        private Object current;

        public LongNumIterator(LList list) {
            this.current = list;
        }

        @Override
        public boolean hasNext() {
            return current instanceof Pair;
        }

        @Override
        public long nextLong() {
            if (!(current instanceof Pair pair)) {
                throw new NoSuchElementException();
            }

            Object value = pair.getCar();
            current = pair.getCdr();

            if (value instanceof IntNum intNum) {
                return intNum.longValue();
            }

            throw new NoSuchElementException("Element is not an IntNum");
        }
    }
}
