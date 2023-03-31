package by.afinny.moneytransfer.openfeign.deposit;

import by.afinny.moneytransfer.dto.CardNumberDto;
import by.afinny.moneytransfer.dto.CreatePaymentDepositDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@FeignClient("DEPOSIT/auth/deposit-cards/")
public interface DepositDebitCardStatementClient {

    @GetMapping("{cardId}/information")
    ResponseEntity<CardNumberDto> getCardNumberByCardId(@PathVariable UUID cardId);

    @PatchMapping("/{clientId}")
    ResponseEntity<Boolean> writeOffSum(@PathVariable UUID clientId,
                                        @RequestBody CreatePaymentDepositDto createPaymentDepositDto);
}
