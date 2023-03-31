package by.afinny.moneytransfer.service.impl;

import by.afinny.moneytransfer.dto.CommissionDto;
import by.afinny.moneytransfer.entity.TransferType;
import by.afinny.moneytransfer.entity.constant.CurrencyCode;
import by.afinny.moneytransfer.entity.constant.TransferTypeName;
import by.afinny.moneytransfer.mapper.CommissionMapper;
import by.afinny.moneytransfer.repository.TransferTypeRepository;
import by.afinny.moneytransfer.service.CommissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommissionServiceImpl implements CommissionService {

    private final TransferTypeRepository transferTypeRepository;
    private final CommissionMapper commissionMapper;

    @Override
    public CommissionDto getCommissionData(TransferTypeName typeName, CurrencyCode currencyCode) {
        log.info(" getCommissionData () method invoke");
        TransferType transferType = getTransferType(typeName,currencyCode);
        return commissionMapper.toCommissionDto(transferType);
    }

    private TransferType getTransferType(TransferTypeName typeName, CurrencyCode currencyCode) {
        return transferTypeRepository.findByTransferTypeNameAndCurrencyCode(typeName, currencyCode).orElseThrow(
                () -> new EntityNotFoundException("Transfer type with type name and currencyCode" + typeName
                        + currencyCode + " wasn't found"));
    }
}