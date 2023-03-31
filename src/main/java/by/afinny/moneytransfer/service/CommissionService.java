package by.afinny.moneytransfer.service;

import by.afinny.moneytransfer.dto.CommissionDto;
import by.afinny.moneytransfer.entity.constant.CurrencyCode;
import by.afinny.moneytransfer.entity.constant.TransferTypeName;

public interface CommissionService {

    CommissionDto getCommissionData (TransferTypeName typeName, CurrencyCode currencyCode);
}
