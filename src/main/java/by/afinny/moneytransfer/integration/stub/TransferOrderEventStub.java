package by.afinny.moneytransfer.integration.stub;

import by.afinny.moneytransfer.dto.kafka.ConsumerTransferOrderEvent;
import by.afinny.moneytransfer.dto.kafka.ProducerTransferOrderEvent;
import by.afinny.moneytransfer.entity.constant.TransferStatus;
import by.afinny.moneytransfer.integration.ConsumerTransferOrderEventListener;
import by.afinny.moneytransfer.integration.ProducerTransferOrderEventSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransferOrderEventStub {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${" + ConsumerTransferOrderEventListener.topicName + ".path}")
    private String topic;

    @KafkaListener(
            topics = "${" + ProducerTransferOrderEventSender.topicName + ".path}",
            groupId = "stub-transfer-order")
    public void getMessage(ProducerTransferOrderEvent producerTransferOrderEvent) {
        ConsumerTransferOrderEvent consumerTransferOrderEvent = createConsumerEvent(producerTransferOrderEvent.getId());
        sendMessage(consumerTransferOrderEvent);
    }

    private void sendMessage(ConsumerTransferOrderEvent consumerTransferOrderEvent) {
        kafkaTemplate.send(topic, consumerTransferOrderEvent);
    }

    private ConsumerTransferOrderEvent createConsumerEvent(UUID id) {
        return ConsumerTransferOrderEvent.builder()
                .id(id)
                .authorizationCode(createAuthorizationCode())
                .createdAt(LocalDateTime.now())
                .status(createTransferStatus())
                .build();
    }

    private String createAuthorizationCode() {
        int stringLength = 20;
        return new Random()
                .ints(stringLength, 65, 123)
                .mapToObj(i -> (char) i)
                .map(Object::toString)
                .collect(Collectors.joining());
    }

    private TransferStatus createTransferStatus() {
        return TransferStatus.values()[(int) (Math.random() * 2 + 2)];
    }
}