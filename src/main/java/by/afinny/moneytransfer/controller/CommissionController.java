package by.afinny.moneytransfer.controller;

import by.afinny.moneytransfer.dto.CommissionDto;
import by.afinny.moneytransfer.entity.constant.CurrencyCode;
import by.afinny.moneytransfer.entity.constant.TransferTypeName;
import by.afinny.moneytransfer.service.CommissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("auth/commission")
@RequiredArgsConstructor
public class CommissionController {

    public static final String URL_COMMISSION = "/auth/commission";
    public static final String PARAM_TYPE_NAME = "typeName";
    public static final String PARAM_CURRENCY_CODE = "currencyCode";

    private final CommissionService commissionService;

    @GetMapping
    public ResponseEntity<CommissionDto> getCommissionData(@RequestParam TransferTypeName typeName,
                                                           @RequestParam CurrencyCode currencyCode) {
        CommissionDto commissionDto = commissionService.getCommissionData(typeName, currencyCode);
        return ResponseEntity.ok(commissionDto);
    }
}
