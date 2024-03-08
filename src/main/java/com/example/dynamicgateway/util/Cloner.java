package com.example.dynamicgateway.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import lombok.SneakyThrows;

/**
 * Utility class with the purpose of creating deep copies of objects by means of
 * a serialization/deserialization cycle.
 * <p>
 * This class delegates the actual serialization/deserialization to an {@link ObjectMapper}
 * dependency with a registered {@link ParameterNamesModule}. Clients must ensure that passed
 * objects meet {@code ObjectMapper}'s expectations. Typically, it means providing getter methods
 * (for serialization) and some object creation mechanism (for deserialization). The latter could be
 * a combination of a no-args constructor and setters or, alternatively, a constructor with args
 * marked as {@link JsonCreator}
 */
public class Cloner {
    private static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new ParameterNamesModule());
    }

    private Cloner() {
    }

    /**
     * Serializes and then deserializes an object, thus producing its deep copy
     *
     * @param object object to copy
     * @param type class of the object
     * @param <T> type of the object to be copied
     * @return deep copy of the object
     * @throws JsonProcessingException if serialization or deserialization fails
     */
    @SneakyThrows
    @SuppressWarnings("JavadocDeclaration")
    public static <T> T deepCopy(T object, Class<T> type) {
        String serializedObject = objectMapper.writeValueAsString(object);
        return objectMapper.readValue(serializedObject, type);
    }
}
