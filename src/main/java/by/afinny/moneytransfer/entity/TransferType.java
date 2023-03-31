package by.afinny.moneytransfer.entity;

import by.afinny.moneytransfer.entity.constant.CurrencyCode;
import by.afinny.moneytransfer.entity.constant.TransferTypeName;
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
import javax.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = TransferType.TABLE_NAME)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter(AccessLevel.PUBLIC)
@ToString
public class TransferType {

    public static final String TABLE_NAME = "transfer_type";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_name", nullable = false)
    private TransferTypeName transferTypeName;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency_code", nullable = false, length = 3)
    private CurrencyCode currencyCode;

    @Column(name = "min_commission", nullable = false, precision = 19, scale = 4)
    private BigDecimal minCommission;

    @Column(name = "max_commission", nullable = false, precision = 19, scale = 4)
    private BigDecimal maxCommission;

    @Column(name = "percent_commission", precision = 19, scale = 4)
    private BigDecimal percentCommission;

    @Column(name = "fix_commission", precision = 19, scale = 4)
    private BigDecimal fixCommission;

    @Column(name = "min_sum", nullable = false, precision = 19, scale = 4)
    private BigDecimal minSum;

    @Column(name = "max_sum", nullable = false, precision = 19, scale = 4)
    private BigDecimal maxSum;
}
