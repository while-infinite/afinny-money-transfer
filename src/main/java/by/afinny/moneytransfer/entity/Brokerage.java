package by.afinny.moneytransfer.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = Brokerage.TABLE_NAME)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Brokerage {

    public static final String TABLE_NAME = "brokerage";

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "brokerage_account_name")
    private String brokerageAccountName;
}
