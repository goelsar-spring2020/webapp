package edu.neu.cloudwebapp.model;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Table(name = "bills")
public class BillDetails {
    @Id
    @Column(nullable = false, unique = true, columnDefinition = "VARCHAR(50)")
    private String id;
    @Column(nullable = false, columnDefinition = "VARCHAR(50)")
    private String owner_id;
    @Column(nullable = false)
    private String vendor;
    @Column(nullable = false)
    private Date created_ts;
    @Column(nullable = false)
    private Date updated_ts;
    @Column(nullable = false)
    private Date bill_date;
    @Column(nullable = false)
    private Date due_date;
    @DecimalMin("0.1")
    @Column(nullable = false, precision = 10, scale = 2, columnDefinition = "Decimal(10,2)")
    private Double amount_due;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;
    @ElementCollection
    @CollectionTable(name = "categories", joinColumns = @JoinColumn(name = "id"))
    @Column(name = "categories")
    private List<String> categories = new ArrayList<>();
}