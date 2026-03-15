package com.taskflow.service;

import com.taskflow.dto.request.AssignProjectMemberRequest;
import com.taskflow.dto.request.CreateProjectRequest;
import com.taskflow.dto.request.UpdateProjectRequest;
import com.taskflow.dto.response.ProjectResponse;

import java.util.List;

public interface ProjectService {
    ProjectResponse createProject(String currentUserEmail, CreateProjectRequest request);
    List<ProjectResponse> getAllProjects(String currentUserEmail);
    ProjectResponse getProjectById(String currentUserEmail, Long id);
    ProjectResponse updateProject(String currentUserEmail, Long id, UpdateProjectRequest request);
    void deleteProject(String currentUserEmail, Long id);
    ProjectResponse assignEmployee(String currentUserEmail, Long projectId, AssignProjectMemberRequest request);
}
