package org.zlambda.sandbox.commons;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public enum JacksonUtils {
    ;

    private static ObjectMapper getObjectMapper() {
        /**
         * configure object mapper only auto detect field
         */
        return new ObjectMapper()
                .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
                .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static String serialize(Object obj) throws Exception {
        return serialize(obj, true);
    }

    public static String serialize(Object object, boolean prettyPrint) throws Exception {
        return getObjectMapper()
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
        return getObjectMapper().readValue(json, type);
    }
}
