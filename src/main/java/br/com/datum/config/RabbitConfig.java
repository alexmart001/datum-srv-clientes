package br.com.datum.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * O exchange e a fila usados para PUBLICAR eventos (datum001.topic /
 * process-datum001) já existem no servidor e são administrados fora desta
 * aplicação - por isso não são declarados aqui (declarar de novo com
 * propriedades diferentes das já criadas quebraria a subida da
 * aplicação).
 *
 * Já a fila usada para CONSUMIR mensagens de alteração de status
 * (customer_status_changed) ainda não existia em lugar nenhum, então essa
 * é declarada em código: idempotente, sem risco de conflito.
 */
@Configuration
public class RabbitConfig {

    @Bean
    public MessageConverter jackson2JsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public Queue customerStatusChangeQueue(@Value("${datum.rabbitmq.customer-status-change-queue}") String queueName) {
        return QueueBuilder.durable(queueName).build();
    }

    /**
     * Container factory dedicada ao CustomerStatusChangeListener, SEM o
     * Jackson2JsonMessageConverter. Por padrão, o Spring Boot aplicaria o
     * único MessageConverter do contexto (o Jackson, acima) a todos os
     * listeners - o que faria a conversão JSON->objeto acontecer *antes*
     * do método do listener ser chamado, quebrando o container inteiro
     * (AmqpRejectAndDontRequeueException) se a mensagem externa vier com
     * um JSON mal formado. Aqui o listener recebe os bytes crus e faz a
     * desserialização manualmente, dentro de um try/catch.
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rawMessageListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        return factory;
    }
}
