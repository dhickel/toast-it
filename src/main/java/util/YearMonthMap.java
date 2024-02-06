package util;

import enums.Month;
import enums.Year;

import java.util.*;
import java.util.concurrent.locks.StampedLock;


public class YearMonthMap<T> {
    private final Map<Year, Map<Month, List<T>>> data = new EnumMap<>(Year.class);

    public YearMonthMap() {
        for (var yr : Year.values()) {
            Map<Month, List<T>> yearMap = new EnumMap<>(Month.class);
            data.put(yr, yearMap);
            for (var mth : Month.values()) {
                yearMap.put(mth, new ArrayList<>());
            }
        }
    }

    public synchronized Map<Month, List<T>> getOfYear(Year year) {
        var yearEntries = data.get(year);
        return yearEntries == null ? Map.of() : yearEntries;
    }

    public synchronized List<T> getOfMonth(Year year, Month month) {
        var monthEntries = data.getOrDefault(year, Map.of()).get(month);
        return monthEntries == null ? List.of() : monthEntries;
    }

    public synchronized void addItem(Year year, Month month, T item) {
        List<T> entries = data.get(year).get(month);
        int existing = entries.indexOf(item);
        if (existing != -1) {
            entries.set(existing, item);
        } else {
            entries.add(item);
        }
    }
}
