package by.afinny.moneytransfer.repository;

import by.afinny.moneytransfer.entity.TransferOrder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TransferOrderRepository extends JpaRepository<TransferOrder, UUID> {

    List<TransferOrder> findByClientIdAndStartDateNotNullAndTransferPeriodicityNotNull(UUID clientId);

    TransferOrder findByClientIdAndId(UUID clientId, UUID transferOrderId);

    @Query("from TransferOrder where remitterCardNumber =:remitterCardNumber and clientId =:clientId and completedAt >=:from and completedAt <=:to and transferStatus ='PERFORMED'")
    List<TransferOrder> findAllByRemitterCardNumber(@Param("remitterCardNumber") String remitterCardNumber,
                                                    @Param("clientId") UUID clientId,
                                                    @Param("from") LocalDateTime from,
                                                    @Param("to") LocalDateTime to,
                                                    Pageable pageable);
}