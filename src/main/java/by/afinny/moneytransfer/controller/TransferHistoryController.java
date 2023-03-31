package by.afinny.moneytransfer.controller;

import by.afinny.moneytransfer.dto.CreditCardStatementDto;
import by.afinny.moneytransfer.dto.DebitCardStatementDto;
import by.afinny.moneytransfer.dto.DetailsHistoryDto;
import by.afinny.moneytransfer.dto.FilterOptionsDto;
import by.afinny.moneytransfer.dto.TransferOrderHistoryDto;
import by.afinny.moneytransfer.service.TransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("auth/history")
@RequiredArgsConstructor
public class TransferHistoryController {

    public static final String URL_HISTORY = "/auth/history";
    public static final String URL_CREDIT = "/credit/{cardId}";
    public final static String URL_DEPOSIT = "/deposit/{cardId}";
    public static final String URL_DETAILS = "/details";
    public static final String PARAM_CLIENT_ID = "clientId";
    public static final String PARAM_FROM = "from";
    public static final String PARAM_TO = "to";
    public static final String PARAM_PAGE_NUMBER = "pageNumber";
    public static final String PARAM_PAGE_SIZE = "pageSize";
    public static final String PARAM_TRANSFER_ORDER_ID = "transferOrderId";

    private final TransferService transferService;

    @GetMapping()
    public ResponseEntity<List<TransferOrderHistoryDto>> getTransferOrderHistory(@Valid FilterOptionsDto filterOptions) {
        List<TransferOrderHistoryDto> transferOrderHistory = transferService.getTransferOrderHistory(filterOptions);
        return ResponseEntity.ok(transferOrderHistory);
    }

    @GetMapping("credit/{cardId}")
    public ResponseEntity<List<CreditCardStatementDto>> getCreditCardStatement(@PathVariable UUID cardId,
                                                                               @RequestParam UUID clientId,
                                                                               @RequestParam String from,
                                                                               @RequestParam String to,
                                                                               @RequestParam Integer pageNumber,
                                                                               @RequestParam Integer pageSize) {
        List<CreditCardStatementDto> creditCardStatement = transferService
                .getCreditCardStatement(cardId, clientId, from, to, pageNumber, pageSize);
        return ResponseEntity.ok(creditCardStatement);
    }

    @GetMapping("deposit/{cardId}")
    public ResponseEntity<List<DebitCardStatementDto>> getViewDebitCardStatement(@PathVariable UUID cardId,
                                                                                 @RequestParam UUID clientId,
                                                                                 @RequestParam String from,
                                                                                 @RequestParam String to,
                                                                                 @RequestParam Integer pageNumber,
                                                                                 @RequestParam Integer pageSize) {
        List<DebitCardStatementDto> debitCardStatement = transferService
                .getViewDebitCardStatement(cardId, clientId, from, to, pageNumber, pageSize);
        return ResponseEntity.ok(debitCardStatement);
    }

    @GetMapping("/details")
    public ResponseEntity<DetailsHistoryDto> getDetailsHistory(@RequestParam UUID clientId,
                                                               @RequestParam UUID transferOrderId) {
        DetailsHistoryDto detailsHistoryDto = transferService.getDetailsHistory(clientId, transferOrderId);
        return ResponseEntity.ok(detailsHistoryDto);
    }
}
