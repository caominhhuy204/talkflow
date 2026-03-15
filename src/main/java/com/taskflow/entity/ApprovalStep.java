package com.taskflow.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "approval_steps")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private InternalRequest request;

    @Column(name = "step_no", nullable = false)
    private Integer stepNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "approver_role", nullable = false, length = 20)
    private Role approverRole;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id")
    private User approver;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ApprovalAction action;

    @Column(length = 1000)
    private String comment;

    @Column(name = "acted_at")
    private LocalDateTime actedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApprovalStepStatus status;
}
