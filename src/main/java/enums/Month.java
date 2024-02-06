package enums;

import java.util.InputMismatchException;


public enum Month {
    JANUARY,
    FEBRUARY,
    MARCH,
    APRIL,
    MAY,
    JUNE,
    JULY,
    AUGUST,
    SEPTEMBER,
    OCTOBER,
    NOVEMBER,
    DECEMBER;

    public static Month fromString(String str) {
        for (var y : Month.values()) {
            if (y.name().contains(str.toUpperCase())) {
                return y;
            }
        }
        throw new InputMismatchException("String did not match year.");
    }
}
