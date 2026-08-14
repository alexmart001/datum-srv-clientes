package br.com.datum.serializer;

/**
 * Conversão entre a representação textual do status ("ACTIVE"/"INACTIVE")
 * usada na API e o campo boolean persistido na entidade Cliente.
 */
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
