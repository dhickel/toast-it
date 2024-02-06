package notes;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


public record NoteEntry (
        String noteName,
        LocalDateTime createdAt,
        List<String> topics,
        UUID uuid,
        Path basePath
) {}
