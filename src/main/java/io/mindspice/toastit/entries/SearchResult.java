package io.mindspice.toastit.entries;

import io.mindspice.toastit.entries.text.TextEntry;


public record SearchResult(
        String matchedLine,
        TextEntry entry
) { }
