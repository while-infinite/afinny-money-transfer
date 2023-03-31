package by.afinny.moneytransfer.dao;

import by.afinny.moneytransfer.dto.FilterOptionsDto;
import by.afinny.moneytransfer.entity.TransferOrder;

import java.util.List;

public interface TransferOrderDao {

    List<TransferOrder> getTransferOrderByFilterOptions(FilterOptionsDto filterOptions);

}
