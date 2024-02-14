package io.mindspice.toastit.util;

import io.mindspice.toastit.enums.NotificationLevel;


public record Tag(
        String tagName,
        String notifyTitle,
        String icon
) {

    public static Tag of(String tagName) {
        return new Tag(tagName, tagName, "dialog-information");
    }

    public static Tag of(String tagName, String notifyTitle) {
        return new Tag(tagName, notifyTitle, "dialog-information");
    }

    public static Tag Default() {
        return new Tag("", "", "dialog-information");
    }
}
