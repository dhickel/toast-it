package io.mindspice.toastit.util;

import io.mindspice.toastit.enums.NotificationLevel;


public record Tag(
        String tagName,
        String notifyTitle,
        String icon
) {

    public static Tag of(String tagName) {
        return new Tag(tagName, tagName, "dialog-info");
    }

    public static Tag of(String tagName, String notifyTitle) {
        return new Tag(tagName, notifyTitle, "dialog-info");
    }

    public static Tag Default() {
        return new Tag("", "", "dialog-info");
    }
}
