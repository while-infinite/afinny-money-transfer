package by.afinny.moneytransfer.integration;

import by.afinny.moneytransfer.dto.kafka.ProducerСreatePaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;


@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty("spring.kafka.topics.create-new-payment-producer.enabled")
public class CreatePaymentSource {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.topics.create-new-payment-producer.path}")
    private String kafkaTopic;

    @EventListener
    public void sendMessageAboutBrokeragePaymentInfo(ProducerСreatePaymentEvent event) {
        log.info("Event " + event + " has been received, sending message...");
        kafkaTemplate.send(
                MessageBuilder
                        .withPayload(event)
                        .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
                        .setHeader(KafkaHeaders.TOPIC, kafkaTopic)
                        .build()
        );
    }
}
