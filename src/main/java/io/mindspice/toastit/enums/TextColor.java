package io.mindspice.toastit.enums;

public enum TextColor {
    RESET("\u001B[0m"),
    DARK_GRAY("\u001B[90m"),
    RED("\u001B[31m"),
    GREEN("\u001B[32m"),
    YELLOW("\u001B[33m"),
    BLUE("\u001B[34m"),
    MAGENTA("\u001B[35m"),
    CYAN("\u001B[36m"),
    LIGHT_GRAY("\u001B[37m");

    private final String code;

    TextColor(String code) {
        this.code = code;
    }

    public String wrap(String s) {
        return this.code + s + RESET.code;
    }
}
