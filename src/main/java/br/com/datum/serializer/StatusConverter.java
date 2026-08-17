package br.com.datum.serializer;

public final class StatusConverter {

    private StatusConverter() {}

    public static boolean toBoolean(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Status value must not be null. Expected 'ACTIVE' or 'INACTIVE'.");
        }

        return switch (value.trim().toUpperCase()) {
            case "ACTIVE" -> true;
            case "INACTIVE" -> false;
            default -> throw new IllegalArgumentException(
                    "Invalid status value: '" + value + "'. Expected 'ACTIVE' or 'INACTIVE'.");
        };
    }
}
