package io.mindspice.toastit.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.mindspice.toastit.notification.Reminder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;


public class JSON {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final TypeReference<List<String>> stringList = new TypeReference<>() { };
    private static final TypeReference<List<Long>> longList = new TypeReference<>() { };
    private static final TypeReference<List<Reminder.Stub>> reminderList = new TypeReference<>() { };

    static {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setVisibility(objectMapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));

        SimpleModule module = new SimpleModule();
        module.addSerializer(Path.class, new PathSerializer());

        // Register the module with the ObjectMapper
        objectMapper.registerModule(module);
    }

    public static String writePretty(Object obj) throws JsonProcessingException {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
    }

    public static String writeString(Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }

    public static <T> T loadObjectFromFile(Path path, Class<T> clazz) throws IOException {
        return JSON.objectMapper.readValue(path.toFile(), clazz);
    }

    public static <T> T loadObjectFromFile(String path, Class<T> clazz) throws IOException {
        return JSON.objectMapper.readValue(new File(path), clazz);
    }

    public static <T> T read(String json, Class<T> clazz) throws JsonProcessingException {
        return objectMapper.readValue(json, clazz);
    }

    public static List<String> jsonArrayToStringList(String arrayString) {
        try {
            return objectMapper.readValue(arrayString, stringList);
        } catch (JsonProcessingException e) {
            System.err.println("Failed converting json of: " + arrayString + " to list");
            return List.of();
        }
    }

    public static List<LocalDateTime> arrayStringToDataTimeList(String arrayString) {
        try {
            List<Long> epochTimes = objectMapper.readValue(arrayString, longList);
            return epochTimes.stream().map(DateTimeUtil::unixToLocal).toList();
        } catch (JsonProcessingException e) {
            System.err.println("Failed converting json of: " + arrayString + " to list");
            return List.of();
        }
    }

    public static List<Long> arrayStringToEpochList(String arrayString) {
        try {
            return objectMapper.readValue(arrayString, longList);
        } catch (JsonProcessingException e) {
            System.err.println("Failed converting json of: " + arrayString + " to list");
            return List.of();
        }
    }

    public static List<Reminder> jsonArrayToReminderList(String arrayString) {
        try {
            return objectMapper.readValue(arrayString, reminderList).stream().map(Reminder.Stub::asFull).toList();
        } catch (JsonProcessingException e) {
            System.err.println("Failed converting json of: " + arrayString + " to list");
            return List.of();
        }
    }

}
