package org.mytest.test.utils;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import java.lang.reflect.Type;
import java.math.BigDecimal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

/**
 * Copy from org.springframework.ai.util.json.JsonParser
 *
 * @author gemo
 * @date 2026/5/27 14:16
 */
public class JsonParser {
    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .addModules(JacksonUtils.instantiateAvailableModules())
            .build();

    private JsonParser() {
    }

    /**
     * Returns a Jackson {@link ObjectMapper} instance tailored for JSON-parsing
     * operations for tool calling and structured output.
     */
    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }

    /**
     * Converts a JSON string to a Java object.
     */
    public static <T> T fromJson(String json, Class<T> type) {
        Assert.notNull(json, "json cannot be null");
        Assert.notNull(type, "type cannot be null");

        try {
            return OBJECT_MAPPER.readValue(json, type);
        }
        catch (JsonProcessingException ex) {
            throw new IllegalStateException(String.format("Conversion from JSON to %s failed", type.getName()), ex);
        }
    }

    /**
     * Converts a JSON string to a Java object.
     */
    public static <T> T fromJson(String json, Type type) {
        Assert.notNull(json, "json cannot be null");
        Assert.notNull(type, "type cannot be null");

        try {
            return OBJECT_MAPPER.readValue(json, OBJECT_MAPPER.constructType(type));
        }
        catch (JsonProcessingException ex) {
            throw new IllegalStateException(String.format("Conversion from JSON to %s failed", type.getTypeName()), ex);
        }
    }

    /**
     * Converts a JSON string to a Java object.
     */
    public static <T> T fromJson(String json, TypeReference<T> type) {
        Assert.notNull(json, "json cannot be null");
        Assert.notNull(type, "type cannot be null");

        try {
            return OBJECT_MAPPER.readValue(json, type);
        }
        catch (JsonProcessingException ex) {
            throw new IllegalStateException(String.format("Conversion from JSON to %s failed", type.getType().getTypeName()),
                    ex);
        }
    }

    /**
     * Converts a Java object to a JSON string.
     */
    public static String toJson(@Nullable Object object) {
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        }
        catch (JsonProcessingException ex) {
            throw new IllegalStateException("Conversion from Object to JSON failed", ex);
        }
    }

    /**
     * Convert a Java Object to a typed Object. Based on the implementation in
     * MethodToolCallback.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static Object toTypedObject(Object value, Class<?> type) {
        Assert.notNull(value, "value cannot be null");
        Assert.notNull(type, "type cannot be null");

        Class<?> javaType = ClassUtils.resolvePrimitiveIfNecessary(type);

        if (javaType == String.class) {
            return value.toString();
        }
        else if (javaType == Byte.class) {
            return Byte.parseByte(value.toString());
        }
        else if (javaType == Integer.class) {
            BigDecimal bigDecimal = new BigDecimal(value.toString());
            return bigDecimal.intValueExact();
        }
        else if (javaType == Short.class) {
            return Short.parseShort(value.toString());
        }
        else if (javaType == Long.class) {
            BigDecimal bigDecimal = new BigDecimal(value.toString());
            return bigDecimal.longValueExact();
        }
        else if (javaType == Double.class) {
            return Double.parseDouble(value.toString());
        }
        else if (javaType == Float.class) {
            return Float.parseFloat(value.toString());
        }
        else if (javaType == Boolean.class) {
            return Boolean.parseBoolean(value.toString());
        }
        else if (javaType.isEnum()) {
            return Enum.valueOf((Class<Enum>) javaType, value.toString());
        }

        String json = JsonParser.toJson(value);
        return JsonParser.fromJson(json, javaType);
    }

    public static JsonNode readTree(String json) {
        try {
            return OBJECT_MAPPER.readTree(json);
        } catch (Exception e) {
            throw new IllegalStateException("Conversion from Object to JSON failed", e);
        }
    }
}
