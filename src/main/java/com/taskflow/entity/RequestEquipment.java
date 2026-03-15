package com.taskflow.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "request_equipment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestEquipment {

    @Id
    @Column(name = "request_id")
    private Long requestId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "request_id")
    private InternalRequest request;

    @Column(name = "item_name", nullable = false, length = 200)
    private String itemName;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "estimated_cost", nullable = false, precision = 14, scale = 2)
    private BigDecimal estimatedCost;

    @Column(name = "business_justification", nullable = false, length = 1000)
    private String businessJustification;
}
