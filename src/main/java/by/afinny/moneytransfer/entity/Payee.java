package by.afinny.moneytransfer.entity;

import by.afinny.moneytransfer.entity.constant.PayeeType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = Payee.TABLE_NAME)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter(AccessLevel.PUBLIC)
@ToString
public class Payee {

    public static final String TABLE_NAME = "payee";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private PayeeType payeeType;

    @Column(name = "name")
    private String name;

    @Column(name = "INN", length = 12)
    private String inn;

    @Column(name = "BIC", nullable = false, length = 9)
    private String bic;

    @Column(name = "payee_account_number", nullable = false)
    private String payeeAccountNumber;

    @Column(name = "payee_card_number")
    private String payeeCardNumber;

    @OneToMany(mappedBy = "payee", cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @Fetch(value = FetchMode.SUBSELECT)
    @ToString.Exclude
    private List<AdditionalParameters> additionalParameters;

}