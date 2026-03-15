package com.taskflow.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "project_code", unique = true, length = 32)
    private String projectCode;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private ProjectStatus status;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private ProjectVisibility visibility;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_manager_id")
    private User ownerManager;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(name = "archived_at")
    private LocalDateTime archivedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (status == null) status = ProjectStatus.PLANNING;
        if (visibility == null) visibility = ProjectVisibility.DEPARTMENT;
    }
}
