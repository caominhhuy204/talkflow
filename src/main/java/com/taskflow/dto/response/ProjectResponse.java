package com.taskflow.dto.response;

import com.taskflow.entity.ProjectStatus;
import com.taskflow.entity.ProjectVisibility;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String projectCode;
    private String name;
    private String description;
    private ProjectStatus status;
    private ProjectVisibility visibility;
    private Long ownerManagerId;
    private String ownerManagerEmail;
    private String departmentCode;
    private Long departmentId;
    private LocalDateTime archivedAt;
    private LocalDateTime createdAt;
}
