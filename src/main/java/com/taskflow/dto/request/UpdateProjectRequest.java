package com.taskflow.dto.request;

import com.taskflow.entity.ProjectStatus;
import com.taskflow.entity.ProjectVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProjectRequest {

    @NotBlank(message = "Project name must not be blank")
    @Size(max = 150, message = "Project name must not exceed 150 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    private ProjectStatus status;

    private ProjectVisibility visibility;
}
