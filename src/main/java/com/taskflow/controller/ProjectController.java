package com.taskflow.controller;

import com.taskflow.dto.request.AssignProjectMemberRequest;
import com.taskflow.dto.request.CreateProjectRequest;
import com.taskflow.dto.request.UpdateProjectRequest;
import com.taskflow.dto.response.ApiResponse;
import com.taskflow.dto.response.ProjectResponse;
import com.taskflow.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PreAuthorize("hasAnyRole('MANAGER','EMPLOYEE','HR')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getAllProjects(Principal principal) {
        List<ProjectResponse> response = projectService.getAllProjects(principal.getName());

        return ResponseEntity.ok(
                ApiResponse.<List<ProjectResponse>>builder()
                        .message("Project list fetched successfully")
                        .data(response)
                        .build()
        );
    }

    @PreAuthorize("hasAnyRole('MANAGER','EMPLOYEE','HR')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> getProjectById(Principal principal, @PathVariable Long id) {
        ProjectResponse response = projectService.getProjectById(principal.getName(), id);

        return ResponseEntity.ok(
                ApiResponse.<ProjectResponse>builder()
                        .message("Project fetched successfully")
                        .data(response)
                        .build()
        );
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(
            Principal principal,
            @Valid @RequestBody CreateProjectRequest request) {

        ProjectResponse response = projectService.createProject(principal.getName(), request);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<ProjectResponse>builder()
                        .message("Project created successfully")
                        .data(response)
                        .build()
        );
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(
            Principal principal,
            @PathVariable Long id,
            @Valid @RequestBody UpdateProjectRequest request) {

        ProjectResponse response = projectService.updateProject(principal.getName(), id, request);

        return ResponseEntity.ok(
                ApiResponse.<ProjectResponse>builder()
                        .message("Project updated successfully")
                        .data(response)
                        .build()
        );
    }

    @PreAuthorize("hasRole('MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteProject(Principal principal, @PathVariable Long id) {
        projectService.deleteProject(principal.getName(), id);

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .message("Project archived successfully")
                        .data("Archived project with id: " + id)
                        .build()
        );
    }

    @PreAuthorize("hasRole('HR')")
    @PostMapping("/{id}/assign-employee")
    public ResponseEntity<ApiResponse<ProjectResponse>> assignEmployee(Principal principal,
                                                                       @PathVariable Long id,
                                                                       @Valid @RequestBody AssignProjectMemberRequest request) {
        ProjectResponse response = projectService.assignEmployee(principal.getName(), id, request);
        return ResponseEntity.ok(
                ApiResponse.<ProjectResponse>builder()
                        .message("Employee assigned to project successfully")
                        .data(response)
                        .build()
        );
    }
}
