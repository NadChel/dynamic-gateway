package com.example.dynamicgateway.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

public class Cloner {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @SneakyThrows
    public static <T> T deepCopy(T object, Class<T> type) {
        String serializedObject = objectMapper.writeValueAsString(object);
        return objectMapper.readValue(serializedObject, type);
    }
}
