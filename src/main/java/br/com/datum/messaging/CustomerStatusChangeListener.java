package br.com.datum.messaging;

import br.com.datum.exception.ResourceNotFoundException;
import br.com.datum.messaging.event.CustomerStatusChangeEvent;
import br.com.datum.serializer.StatusConverter;
import br.com.datum.services.ClienteServices;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;

@Component
public class CustomerStatusChangeListener {

    private static final Logger logger = LoggerFactory.getLogger(CustomerStatusChangeListener.class);
    private static final String EXPECTED_EVENT_TYPE = "CUSTOMER_STATUS_CHANGE";

    private final ObjectMapper objectMapper;
    private final ClienteServices clienteServices;

    public CustomerStatusChangeListener(ObjectMapper objectMapper, ClienteServices clienteServices) {
        this.objectMapper = objectMapper;
        this.clienteServices = clienteServices;
    }

    @RabbitListener(
            queues = "${datum.rabbitmq.customer-status-change-queue}",
            containerFactory = "rawMessageListenerContainerFactory")
    public void handle(Message message) {
        String body = new String(message.getBody(), StandardCharsets.UTF_8);

        CustomerStatusChangeEvent event;
        try {
            event = objectMapper.readValue(body, CustomerStatusChangeEvent.class);
        } catch (Exception ex) {
            logger.error("Mensagem descartada: JSON inválido. payload={}", body, ex);
            return;
        }

        if (!EXPECTED_EVENT_TYPE.equals(event.getEventType())) {
            logger.warn("Mensagem ignorada: eventType '{}' inesperado (esperado '{}'). eventId={}",
                    event.getEventType(), EXPECTED_EVENT_TYPE, event.getEventId());
            return;
        }

        if (event.getCustomerId() == null || !StringUtils.hasText(event.getStatus())) {
            logger.error("Mensagem descartada: customerId/status ausente. evento={}", event);
            return;
        }

        try {
            boolean statusValue = StatusConverter.toBoolean(event.getStatus());
            clienteServices.updateStatus(event.getCustomerId(), statusValue);
            logger.info("Status do cliente id={} atualizado para '{}' via evento {} (eventId={})",
                    event.getCustomerId(), event.getStatus(), EXPECTED_EVENT_TYPE, event.getEventId());
        } catch (ResourceNotFoundException ex) {
            logger.error("Mensagem descartada: cliente id={} não encontrado. eventId={}",
                    event.getCustomerId(), event.getEventId());
        } catch (IllegalArgumentException ex) {
            logger.error("Mensagem descartada: status '{}' inválido. eventId={}",
                    event.getStatus(), event.getEventId());
        } catch (Exception ex) {
            logger.error("Falha inesperada ao processar evento. eventId={}", event.getEventId(), ex);
        }
    }
}
