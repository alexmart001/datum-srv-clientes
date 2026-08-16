package br.com.datum.messaging.event;

import java.io.Serial;
import java.io.Serializable;

/**
 * Payload recebido na fila de mensagens de alteração de status,
 * publicado por um sistema externo. Formato:
 * {
 *   "eventId": "cbca5352-22ad-48f2-aaf2-704735bc7737",
 *   "eventType": "CUSTOMER_STATUS_CHANGE",
 *   "customerId": 123,
 *   "status": "INACTIVE"
 * }
 *
 * Bean mutável (getters/setters), para o Jackson conseguir desserializar
 * o JSON recebido sem depender de um construtor específico.
 */
public class CustomerStatusChangeEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String eventId;
    private String eventType;
    private Long customerId;
    private String status;

    public CustomerStatusChangeEvent() {}

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "CustomerStatusChangeEvent{" +
                "eventId='" + eventId + '\'' +
                ", eventType='" + eventType + '\'' +
                ", customerId=" + customerId +
                ", status='" + status + '\'' +
                '}';
    }
}
