package ent.entity;

import lombok.*;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;
    @Column(name = "seller_name")
    private String sellerName;
    @Column(name = "seller_id")
    private String sellerId;
    @Column(name = "employee_name")
    private String EmployeeName;
    @Column(name = "employee_id")
    private String EmployeeId;
    private Long amount;
    @Column(name = "transaction_time")
    private Date transactionTime;
}
