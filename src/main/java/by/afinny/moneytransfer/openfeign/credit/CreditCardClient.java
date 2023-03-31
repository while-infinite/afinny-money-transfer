package by.afinny.moneytransfer.openfeign.credit;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient("CREDIT/auth/credit-cards")
public interface CreditCardClient {

    @GetMapping("{cardId}/information")
    ResponseEntity<String> getCardNumber(@PathVariable UUID cardId);
}
