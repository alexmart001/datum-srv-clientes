package br.com.datum.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class StatusDeserializer extends JsonDeserializer<Boolean> {
    @Override
    public Boolean deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getText();
        if (value == null) {
            return null;
        }

        return switch (value.trim().toUpperCase()) {
            case "ACTIVE" -> true;
            case "INACTIVE" -> false;
            default -> throw new IllegalArgumentException(
                    "Invalid status value: '" + value + "'. Expected 'ACTIVE' or 'INACTIVE'.");
        };
    }
}
