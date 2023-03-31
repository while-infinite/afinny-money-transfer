package by.afinny.moneytransfer.integration;

import by.afinny.moneytransfer.dto.kafka.ConsumerTransferOrderEvent;
import by.afinny.moneytransfer.service.TransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static by.afinny.moneytransfer.integration.ConsumerTransferOrderEventListener.topicName;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(topicName + ".enabled")
public class ConsumerTransferOrderEventListener {

    private final TransferService transferService;
    public final static String topicName = "spring.kafka.topics.consumer-transfer-order-event-listener";
    public final static String groupId = "transfer-order";

    @KafkaListener(
            topics = "${" + topicName + ".path}",
            groupId = groupId)
    public void listenConsumerTransferOrderEvent(ConsumerTransferOrderEvent consumerTransferOrderEvent) {
        log.info("Processing event: " + consumerTransferOrderEvent);
        transferService.modifyTransferOrder(consumerTransferOrderEvent);
    }
}