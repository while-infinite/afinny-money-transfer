package by.afinny.moneytransfer.integration.stub;

import by.afinny.moneytransfer.dto.kafka.ConsumerСreatePaymentEvent;
import by.afinny.moneytransfer.dto.kafka.ProducerСreatePaymentEvent;
import by.afinny.moneytransfer.entity.constant.TransferStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
@Slf4j
public class CreatePaymentStub {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.topics.create-new-payment-listener.path}")
    private String topic;

    @KafkaListener(
            topics = "${spring.kafka.topics.create-new-payment-producer.path}",
            groupId = "transfer-service"
    )
    public void receiveProducerAndSendConsumerBrokeragePaymentEvent(Message<ProducerСreatePaymentEvent> message) {
        log.info("receiveProducerAndSendConsumerBrokeragePaymentEvent() method invoked");
        ProducerСreatePaymentEvent producerСreatePaymentEvent = message.getPayload();
        ConsumerСreatePaymentEvent consumerСreatePaymentEvent = setUpEvent(producerСreatePaymentEvent);
        sendEvent(consumerСreatePaymentEvent);
    }

    private ConsumerСreatePaymentEvent setUpEvent(ProducerСreatePaymentEvent producerEvent) {
        log.debug("setUpEvent() method invoked");
        TransferStatus transferStatus = checkTransferOrder(producerEvent);
        return ConsumerСreatePaymentEvent.builder()
                .clientId(producerEvent.getClientId())
                .transferOrderId(producerEvent.getTransferOrderId())
                .transferStatus(transferStatus)
                .completedAt(LocalDateTime.now())
                .authorizationCode(createAuthorizationCode())
                .remitterCardNumber(producerEvent.getRemitterCardNumber())
                .sum(producerEvent.getSum())
                .build();
    }

    private TransferStatus checkTransferOrder(ProducerСreatePaymentEvent producerEvent) {
        if (producerEvent.getSum().compareTo(producerEvent.getTransferType().getMinSum()) != -1 &&
                producerEvent.getSum().compareTo(producerEvent.getTransferType().getMaxSum()) != 1) {
            return TransferStatus.PERFORMED;
        } else {
            return TransferStatus.REJECTED;
        }
    }

    private String createAuthorizationCode() {
        int stringLength = 20;
        return new Random()
                .ints(stringLength, 65, 123)
                .mapToObj(i -> (char) i)
                .map(Object::toString)
                .collect(Collectors.joining());
    }

    private void sendEvent(ConsumerСreatePaymentEvent event) {
        log.debug("sendEvent() method invoked");
        log.debug("send event: " + event);
        kafkaTemplate.send(
                MessageBuilder
                        .withPayload(event)
                        .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
                        .setHeader(KafkaHeaders.TOPIC, topic)
                        .build());
    }

}
