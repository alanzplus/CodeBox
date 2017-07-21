package org.alanzplus.codebox.commons.json.jackson;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;

public enum J {
  ;

  public static ObjectMapper newDefaultOM() {
    return newDefaultOM(true);
  }

  public static ObjectMapper newDefaultOM(boolean pretty) {
    return new ObjectMapper()
        /**
         * configure object mapper only to auto detect field
         */
        .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
        .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
        .setSerializationInclusion(JsonInclude.Include.NON_NULL)
        .configure(SerializationFeature.INDENT_OUTPUT, pretty)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  public static String serialize(Object obj) throws JsonProcessingException {
    return newDefaultOM().writeValueAsString(obj);
  }

  public static <T> T deserialize(String json, Class<T> type) throws IOException {
    return newDefaultOM().readValue(json, type);
  }

  public static String prettyPrint(Object obj) {
    try {
      return newDefaultOM(true).writeValueAsString(obj);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException(e);
    }
  }

  /**
   * Unsafe downcast
   */
  public static <T extends JsonNode> ArrayNode arr(T node) {
    return (ArrayNode) node;
  }

  /**
   * Unsafe downcast
   */
  public static <T extends JsonNode> ObjectNode obj(T node) {
    return (ObjectNode) node;
  }
}
