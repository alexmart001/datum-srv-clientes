package br.com.datum.messaging.event;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class CustomerCreatedEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String eventId;
    private final String eventType;
    private final Long customerId;
    private final Instant createdAt;

    public CustomerCreatedEvent(Long customerId) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = "CUSTOMER_CREATED";
        this.customerId = customerId;
        this.createdAt = Instant.now().truncatedTo(ChronoUnit.SECONDS);
    }

    public String getEventId() {
        return eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "CustomerCreatedEvent{" +
                "eventId='" + eventId + '\'' +
                ", eventType='" + eventType + '\'' +
                ", customerId=" + customerId +
                ", createdAt=" + createdAt +
                '}';
    }
}
