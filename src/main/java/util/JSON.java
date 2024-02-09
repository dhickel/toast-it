package util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;


public class JSON {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final TypeReference<Set<String>> setRef = new TypeReference<>() { };

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

    public static Set<String> arrayStringToSet(String arrayString)   {
        try {
            return objectMapper.readValue(arrayString, setRef);
        } catch (JsonProcessingException e) {
            System.out.println("Failed converting json of: " + arrayString +" to set");
            return Set.of();
        }
    }

}
