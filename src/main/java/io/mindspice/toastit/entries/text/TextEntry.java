package io.mindspice.toastit.entries.text;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.mindspice.mindlib.data.tuples.Pair;
import io.mindspice.toastit.entries.DatedEntry;
import io.mindspice.toastit.entries.Entry;
import io.mindspice.toastit.enums.EntryType;
import io.mindspice.toastit.util.DateTimeUtil;
import io.mindspice.toastit.util.JSON;
import io.mindspice.toastit.util.Util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;


public record TextEntry(
        EntryType entryType,
        String name,
        LocalDateTime createdAt,
        List<String> tags,
        UUID uuid,
        Path basePath
) implements Entry, DatedEntry {

    public TextEntry {
        createdAt = createdAt.truncatedTo(ChronoUnit.MINUTES);
        try {
            basePath = basePath == null ? Util.getEntriesPath(entryType) : basePath;
        } catch (IOException e) {
            System.err.println("Error creating path for: " + this);
        }

    }

    public void flushToDisk() {
        if (!Files.exists(basePath)) {
            throw new IllegalStateException("Failed to update on disk, base path does not exists: " + basePath);
        }
        // Always write meta file on change
        Path metaFilePath = basePath.resolve(uuid + "." + entryType.name().toLowerCase() + ".meta");
        try {
            String metaJson = JSON.writePretty(this);
            Files.writeString(metaFilePath, metaJson);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write "
                    + entryType.name().toLowerCase()
                    + " meta file: " + metaFilePath, e);
        }

        // create note file if it doesn't exist
        Path noteFilePath = basePath.resolve(uuid + "." + entryType.name().toLowerCase());
        if (!Files.exists(noteFilePath)) {
            try {
                Files.createFile(noteFilePath);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to write "
                        + entryType.name().toLowerCase()
                        + " file: " + metaFilePath, e);
            }
        }
    }

    public Path getMetaPath() {
        return basePath.resolve(uuid + "." + entryType.name().toLowerCase() + ".meta");
    }

    public Path getFilePath() {
        return basePath.resolve(uuid + "." + entryType.name().toLowerCase());
    }

    public Stub getStub() throws JsonProcessingException {
        return new Stub(
                uuid.toString(),
                name,
                createdAt.atZone(ZoneId.systemDefault()).toInstant().getEpochSecond(),
                JSON.writeString(tags),
                basePath.toString()
        );
    }

    public static Builder builder(EntryType entryType) {
        if (entryType != EntryType.NOTE && entryType != EntryType.JOURNAL) {
            throw new IllegalStateException("Attempted to create invalid entry: " + entryType
                    + ", Valid types: " + EntryType.NOTE + " " + EntryType.JOURNAL);
        }
        return new Builder(entryType);
    }

    public Builder updateBuilder() {
        return new Builder(this);
    }

    @Override
    public String description() {
        return name;
    }

    @Override
    public EntryType type() {
        return entryType;
    }

    @Override
    public LocalDateTime startedAt() {
        return createdAt;
    }

    @Override
    public LocalDateTime dueBy() {
        return createdAt;
    }

    @Override
    public LocalDateTime completedAt() {
        return createdAt;
    }

    @Override
    public boolean completed() {
        return true;
    }

    public static class Builder {
        private final EntryType entryType;
        public String name = "";
        public LocalDateTime createdAt = DateTimeUtil.MAX;
        public List<String> tags = new ArrayList<>();
        private UUID uuid = UUID.randomUUID();
        public Path basePath;

        public Builder(EntryType entryType) {
            this.entryType = entryType;
        }

        public Builder(TextEntry n) {
            this.entryType = n.entryType;
            this.name = n.name;
            this.createdAt = n.createdAt;
            this.tags = new ArrayList<>(n.tags);
            this.uuid = n.uuid;
            this.basePath = n.basePath;
        }

        public TextEntry build() throws IOException {
            if (basePath == null) {
                basePath = Util.getEntriesPath(entryType);
            }

            return new TextEntry(
                    entryType,
                    name,
                    createdAt,
                    Collections.unmodifiableList(tags),
                    uuid,
                    basePath
            );
        }

        public List<Pair<String, String>> toTableState() {
            List<Pair<String, String>> rntList = new ArrayList<>(5);
            if (!name.isEmpty()) {
                rntList.add(Pair.of("Name", name));
            }
            if (!tags.isEmpty()) {
                rntList.add(Pair.of("Tags", tags.toString()));
            }
            return rntList;
        }
    }


    public record Stub(
            String uuid,
            String name,
            long createdAt,
            String tags,
            String metaPath
    ) {
        public TextEntry getAsFull(EntryType entryType) {
            return new TextEntry(
                    entryType,
                    name,
                    DateTimeUtil.unixToLocal(createdAt),
                    JSON.jsonArrayToStringList(tags),
                    UUID.fromString(uuid),
                    Path.of(metaPath)
            );
        }
    }
}
