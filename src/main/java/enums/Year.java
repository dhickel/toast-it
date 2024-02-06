package enums;

import java.util.InputMismatchException;


public enum Year {
    _2024,
    _2025,
    _2026,
    _2027,
    _2028,
    _2029,
    _2030,
    _2031,
    _2032,
    _2033,
    _2034;

    public static Year fromString(String str) {
        for (var y : Year.values()) {
            if (y.name().contains(str)) {
                return y;
            }
        }
        throw new InputMismatchException("String did not match year.");
    }
}
