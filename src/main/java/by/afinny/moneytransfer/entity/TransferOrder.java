package by.afinny.moneytransfer.entity;

import by.afinny.moneytransfer.entity.constant.OperationType;
import by.afinny.moneytransfer.entity.constant.TransferPeriodicity;
import by.afinny.moneytransfer.entity.constant.TransferStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = TransferOrder.TABLE_NAME)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter(AccessLevel.PUBLIC)
@ToString
public class TransferOrder {

    public static final String TABLE_NAME = "transfer_order";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID id;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(optional = false)
    @JoinColumn(name = "transfer_type_id", nullable = false)
    private TransferType transferType;

    @Column(name = "purpose")
    private String purpose;

    @Column(name = "remitter_card_number", nullable = false, length = 16)
    private String remitterCardNumber;

    @ManyToOne(optional = false)
    @JoinColumn(name = "payee_id", nullable = false)
    private Payee payee;

    @Column(name = "sum", nullable = false, precision = 19, scale = 4)
    private BigDecimal sum;

    @Column(name = "sum_commission", precision = 19, scale = 4)
    private BigDecimal sumCommission;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransferStatus transferStatus;

    @Column(name = "authorization_code", nullable = false)
    private String authorizationCode;

    @Column(name = "currency_exchange", nullable = false, precision = 19, scale = 4)
    private BigDecimal currencyExchange;

    @Column(name = "is_favorite")
    private Boolean isFavorite;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "periodicity")
    private TransferPeriodicity transferPeriodicity;

    @Column(name = "client_id")
    private UUID clientId;

    @Column(name = "operation_type")
    @Enumerated(EnumType.STRING)
    private OperationType operationType;

    @ManyToOne
    @JoinColumn(name = "brokerage_id")
    private Brokerage brokerage;
}
