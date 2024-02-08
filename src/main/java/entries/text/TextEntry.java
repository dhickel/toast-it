package entries.text;

import com.fasterxml.jackson.core.JsonProcessingException;
import enums.EntryType;
import util.JSON;
import util.Util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;


public record TextEntry(
        EntryType entryType,
        String name,
        LocalDateTime createdAt,
        Set<String> tags,
        UUID uuid,
        Path basePath
) {

    public void flushToDisk() {
        if (!Files.exists(basePath)) {
            throw new IllegalStateException("Failed to update on disk, base path does not exists: " + basePath);
        }
        // Always write meta file on change
        Path metaFilePath = basePath.resolve(uuid + "." + entryType.name().toLowerCase() + "meta");
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
        return basePath.resolve(uuid + "." + entryType.name().toLowerCase() + "meta");
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
                getMetaPath().toString()
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

    public static class Builder {
        private final EntryType entryType;
        private String name = "Unnamed";
        private LocalDateTime createdAt = LocalDateTime.now();
        private Set<String> tags = new HashSet<>();
        private UUID uuid = UUID.randomUUID();
        private Path basePath;

        public Builder(EntryType entryType) {
            this.entryType = entryType;
        }

        public Builder(TextEntry n) {
            this.entryType = n.entryType;
            this.name = n.name;
            this.createdAt = n.createdAt;
            this.tags = n.tags;
            this.uuid = n.uuid;
            this.basePath = n.basePath;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder setTags(Set<String> tags) {
            this.tags = tags;
            return this;
        }

        public Builder addTag(String tag) {
            this.tags.add(tag);
            return this;
        }

        public TextEntry build() {
            if (basePath == null) {
                Path directoryPath = Util.getEntriesPath(
                        EntryType.PROJECT,
                        LocalDateTime.now().getYear(),
                        LocalDateTime.now().getMonth()
                );
                if (!Files.exists(directoryPath)) {
                    throw new IllegalStateException("Could not resolve path of: " + directoryPath);
                }
                basePath = directoryPath;
            }

            return new TextEntry(
                    entryType,
                    name,
                    createdAt,
                    Collections.unmodifiableSet(tags),
                    uuid,
                    basePath
            );
        }
    }


    public record Stub(
            String uuid,
            String name,
            long createdAt,
            String tags,
            String metaPath
    ) {

    }
}
