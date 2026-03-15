package com.taskflow.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "request_expense")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestExpense {

    @Id
    @Column(name = "request_id")
    private Long requestId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "request_id")
    private InternalRequest request;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private CurrencyCode currency;

    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ExpenseCategory category;

    @Column(nullable = false, length = 1000)
    private String description;
}
