package football.underground.app.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

class ObjectSerializer {

    private final ObjectMapper objectMapper;

    ObjectSerializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    String asString(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Cannot serialize object", e);
        }
    }

    Object fromString(String payloadType, String payload) {
        try {
            var type = Class.forName(payloadType);
            return objectMapper.readValue(payload, type);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Cannot serialize object", e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Cannot find class for name " + payloadType, e);
        }
    }
}
