package br.com.datum.messaging;

import br.com.datum.messaging.event.CustomerCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CustomerEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(CustomerEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    @Value("${datum.rabbitmq.customer-events-exchange}")
    private String exchange;

    @Value("${datum.rabbitmq.customer-created-routing-key}")
    private String customerCreatedRoutingKey;

    public CustomerEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishCustomerCreated(Long customerId) {
        CustomerCreatedEvent event = new CustomerCreatedEvent(customerId);
        try {
            rabbitTemplate.convertAndSend(exchange, customerCreatedRoutingKey, event);
            logger.info("Evento CUSTOMER_CREATED publicado: {}", event);
        } catch (AmqpException ex) {
            logger.error("Falha ao publicar evento CUSTOMER_CREATED para o cliente id={}: {}",
                    customerId, ex.getMessage(), ex);
        }
    }
}
