package by.afinny.moneytransfer.openfeign.investments;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient("INVESTMENTS/auth/investment")
public interface InvestmentsInvestmentClient {
    @GetMapping("/brokerage")
    ResponseEntity<String> getBrokerageAccountName(@RequestParam UUID brokerageAccountId);
}
