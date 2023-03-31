package by.afinny.moneytransfer.integration;

import by.afinny.moneytransfer.dto.kafka.ProducerTransferOrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import static by.afinny.moneytransfer.integration.ProducerTransferOrderEventSender.topicName;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(topicName + ".enabled")
public class ProducerTransferOrderEventSender {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    public final static String topicName = "spring.kafka.topics.producer-transfer-order-event-sender";
    @Value("${" + topicName + ".path}")
    private String topic;

    @EventListener
    public void sendProducerTransferOrderEvent(ProducerTransferOrderEvent producerTransferOrderEvent) {
        log.info("Dto " + producerTransferOrderEvent + " has been received, sending message...");
        kafkaTemplate.send(topic, producerTransferOrderEvent);
    }
}