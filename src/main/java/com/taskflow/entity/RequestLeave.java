package com.taskflow.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "request_leave")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestLeave {

    @Id
    @Column(name = "request_id")
    private Long requestId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "request_id")
    private InternalRequest request;

    @Enumerated(EnumType.STRING)
    @Column(name = "leave_type", nullable = false)
    private LeaveType leaveType;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(nullable = false, length = 1000)
    private String reason;

    @Column(name = "handover_note", length = 1000)
    private String handoverNote;
}
