package by.afinny.moneytransfer.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = TemplateForPayment.TABLE_NAME)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter(AccessLevel.PUBLIC)
@ToString
public class TemplateForPayment {

    public static final String TABLE_NAME = "template_for_payment";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "payee_name", nullable = false)
    private String payeeName;

    @Column(name = "payee_account_number", nullable = false)
    private String payeeAccountNumber;

    @Column(name = "template_purpose_of_payment", nullable = false)
    private String templatePurposeOfPayment;

    @Column(name = "BIC", nullable = false, length = 9)
    private String bic;

    @Column(name = "INN", nullable = false, length = 12)
    private String inn;

}
