package by.afinny.moneytransfer.integration;

import by.afinny.moneytransfer.dto.kafka.ConsumerСreatePaymentEvent;
import by.afinny.moneytransfer.service.TransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Slf4j
public class CreatePaymentListener {

    private final TransferService transferService;

    @KafkaListener(
            topics = "${spring.kafka.topics.create-new-payment-listener.path}",
            groupId = "transfer-service"
    )
    public void onRequestUpdateBrokeragePayment(Message<ConsumerСreatePaymentEvent> message) {

        ConsumerСreatePaymentEvent event = message.getPayload();
        log.info("Processing event: ", event);
        transferService.updatePaymentOrTransfer(event);

    }
}
