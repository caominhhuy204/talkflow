package com.taskflow.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "request_overtime")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestOvertime {

    @Id
    @Column(name = "request_id")
    private Long requestId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "request_id")
    private InternalRequest request;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "total_hours", nullable = false, precision = 6, scale = 2)
    private BigDecimal totalHours;

    @Column(nullable = false, length = 1000)
    private String reason;
}
