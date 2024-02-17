package io.mindspice.toastit.entries;

import io.mindspice.toastit.enums.EntryType;

import java.util.List;


public interface Entry {
    String name();
    List<String> tags();
    EntryType type();
}
