package org.zlambda.codebox.commons;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public enum JacksonUtils {
    ;

    private static ObjectMapper createOM() {
        /**
         * configure object mapper only auto detect field
         */
        return new ObjectMapper()
                .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
                .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static ObjectMapper newOM() {
        return createOM();
    }

    public static String serialize(Object obj) throws JsonProcessingException {
        return serialize(obj, false);
    }

    public static String serialize(Object object, boolean prettyPrint) throws JsonProcessingException {
        return createOM()
                .configure(SerializationFeature.INDENT_OUTPUT, prettyPrint)
                .writeValueAsString(object);
    }

    public static String toPrettyJson(Object object) {
        try {
            return serialize(object);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static <T> T deserialize(String json, Class<T> type) throws Exception {
        return createOM().readValue(json, type);
    }
}
