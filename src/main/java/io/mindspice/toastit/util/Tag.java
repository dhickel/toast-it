package io.mindspice.toastit.util;

public record Tag(
        String tagName,
        String notifyTitle,
        String primaryIcon,
        String secondaryIcon
) {

    public static Tag of(String tagName) {
        return new Tag(tagName, tagName, "dialog-info", "dialog-info");
    }

    public static Tag of(String tagName, String notifyTitle) {
        return new Tag(tagName, notifyTitle, "dialog-info", "dialog-info");
    }

    public static Tag Default() {
        return new Tag("", "", "dialog-info", "dialog-info");
    }

    ;
}
