package by.afinny.moneytransfer.mapper;

import by.afinny.moneytransfer.dto.CommissionDto;
import by.afinny.moneytransfer.entity.TransferType;
import org.mapstruct.Mapper;

@Mapper
public interface CommissionMapper {

    CommissionDto toCommissionDto (TransferType transferType);
}
