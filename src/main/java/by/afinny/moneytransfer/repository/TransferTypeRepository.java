package by.afinny.moneytransfer.repository;

import by.afinny.moneytransfer.entity.TransferType;
import by.afinny.moneytransfer.entity.constant.CurrencyCode;
import by.afinny.moneytransfer.entity.constant.TransferTypeName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransferTypeRepository extends JpaRepository<TransferType, Integer> {

    Optional<TransferType> findByTransferTypeNameAndCurrencyCode(TransferTypeName transferTypeName,
                                                                   CurrencyCode currencyCode);
}