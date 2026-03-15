package com.taskflow.util;

import com.taskflow.dto.response.ProjectResponse;
import com.taskflow.entity.Project;

public final class ProjectMapper {

    private ProjectMapper() {
    }

    public static ProjectResponse toResponse(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .projectCode(project.getProjectCode())
                .name(project.getName())
                .description(project.getDescription())
                .status(project.getStatus())
                .visibility(project.getVisibility())
                .ownerManagerId(project.getOwnerManager() != null ? project.getOwnerManager().getId() : null)
                .ownerManagerEmail(project.getOwnerManager() != null ? project.getOwnerManager().getEmail() : null)
                .departmentId(project.getDepartment() != null ? project.getDepartment().getId() : null)
                .departmentCode(project.getDepartment() != null ? project.getDepartment().getCode() : null)
                .archivedAt(project.getArchivedAt())
                .createdAt(project.getCreatedAt())
                .build();
    }
}
