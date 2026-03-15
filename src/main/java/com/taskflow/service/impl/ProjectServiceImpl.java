package com.taskflow.service.impl;

import com.taskflow.dto.request.AssignProjectMemberRequest;
import com.taskflow.dto.request.CreateProjectRequest;
import com.taskflow.dto.request.UpdateProjectRequest;
import com.taskflow.dto.response.ProjectResponse;
import com.taskflow.entity.*;
import com.taskflow.exception.BadRequestException;
import com.taskflow.exception.ConflictException;
import com.taskflow.exception.ForbiddenOperationException;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.repository.AuditLogRepository;
import com.taskflow.repository.ProjectMemberRepository;
import com.taskflow.repository.ProjectRepository;
import com.taskflow.repository.UserRepository;
import com.taskflow.service.ProjectService;
import com.taskflow.util.ProjectMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;

    public ProjectServiceImpl(ProjectRepository projectRepository,
                              ProjectMemberRepository projectMemberRepository,
                              UserRepository userRepository,
                              AuditLogRepository auditLogRepository) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    @CacheEvict(value = "projects", allEntries = true)
    public ProjectResponse createProject(String currentUserEmail, CreateProjectRequest request) {
        User actor = getUserByEmail(currentUserEmail);
        ensureManager(actor);

        validateUniqueNameInDepartment(actor.getDepartment() != null ? actor.getDepartment().getId() : null, request.getName(), null);

        Project project = Project.builder()
                .name(request.getName().trim())
                .projectCode(generateProjectCode())
                .description(request.getDescription())
                .status(ProjectStatus.PLANNING)
                .visibility(request.getVisibility() != null ? request.getVisibility() : ProjectVisibility.DEPARTMENT)
                .ownerManager(actor)
                .department(actor.getDepartment())
                .build();

        Project savedProject = projectRepository.save(project);
        ensureMember(savedProject, actor, ProjectMemberRole.MANAGER);
        log(savedProject, actor, "CREATE_PROJECT", "Project created");
        return ProjectMapper.toResponse(savedProject);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "projects", key = "#currentUserEmail")
    public List<ProjectResponse> getAllProjects(String currentUserEmail) {
        User actor = getUserByEmail(currentUserEmail);
        ensureNonAdmin(actor);

        Set<Long> memberProjectIds = projectMemberRepository.findByUser_Id(actor.getId())
                .stream()
                .map(member -> member.getProject().getId())
                .collect(Collectors.toSet());

        List<ProjectResponse> response = projectRepository.findAll()
                .stream()
                .filter(project -> canView(actor, project, memberProjectIds))
                .sorted(Comparator.comparing(Project::getCreatedAt).reversed())
                .map(ProjectMapper::toResponse)
                .toList();

        return new ArrayList<>(response);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "project", key = "#currentUserEmail + ':' + #id")
    public ProjectResponse getProjectById(String currentUserEmail, Long id) {
        User actor = getUserByEmail(currentUserEmail);
        ensureNonAdmin(actor);

        Project project = getProject(id);
        boolean isMember = projectMemberRepository.existsByProject_IdAndUser_Id(id, actor.getId());
        if (!canView(actor, project, isMember)) {
            throw new ForbiddenOperationException("You cannot view this project");
        }

        return ProjectMapper.toResponse(project);
    }

    @Override
    @CacheEvict(value = {"projects", "project"}, allEntries = true)
    public ProjectResponse updateProject(String currentUserEmail, Long id, UpdateProjectRequest request) {
        User actor = getUserByEmail(currentUserEmail);
        ensureManager(actor);

        Project project = getProject(id);
        ensureOwnerManager(actor, project);

        ProjectStatus currentStatus = project.getStatus() != null ? project.getStatus() : ProjectStatus.PLANNING;
        ProjectStatus targetStatus = request.getStatus() != null ? request.getStatus() : currentStatus;
        if (currentStatus == ProjectStatus.COMPLETED || currentStatus == ProjectStatus.ARCHIVED) {
            if (targetStatus != ProjectStatus.ARCHIVED || project.getStatus() == ProjectStatus.ARCHIVED) {
                throw new ConflictException("Completed/archived project can only be archived once");
            }
            project.setStatus(ProjectStatus.ARCHIVED);
            project.setArchivedAt(LocalDateTime.now());
            Project updated = projectRepository.save(project);
            log(updated, actor, "ARCHIVE_PROJECT", "Project archived from completed state");
            return ProjectMapper.toResponse(updated);
        }

        if (request.getName() != null && !request.getName().isBlank()) {
            String normalizedName = request.getName().trim();
            validateUniqueNameInDepartment(project.getDepartment() != null ? project.getDepartment().getId() : null, normalizedName, id);
            project.setName(normalizedName);
        }
        project.setDescription(request.getDescription());

        if (request.getVisibility() != null) {
            project.setVisibility(request.getVisibility());
        }

        if (request.getStatus() != null) {
            ensureValidStatusTransition(currentStatus, request.getStatus());
            project.setStatus(request.getStatus());
            if (request.getStatus() == ProjectStatus.ARCHIVED) {
                project.setArchivedAt(LocalDateTime.now());
            }
        }

        Project updatedProject = projectRepository.save(project);
        log(updatedProject, actor, "UPDATE_PROJECT", "Project updated");
        return ProjectMapper.toResponse(updatedProject);
    }

    @Override
    @CacheEvict(value = {"projects", "project"}, allEntries = true)
    public void deleteProject(String currentUserEmail, Long id) {
        User actor = getUserByEmail(currentUserEmail);
        ensureManager(actor);

        Project project = getProject(id);
        ensureOwnerManager(actor, project);
        if (project.getStatus() == ProjectStatus.ARCHIVED) {
            throw new ConflictException("Project is already archived");
        }

        project.setStatus(ProjectStatus.ARCHIVED);
        project.setArchivedAt(LocalDateTime.now());
        projectRepository.save(project);
        log(project, actor, "ARCHIVE_PROJECT", "Project archived");
    }

    @Override
    @Transactional
    @CacheEvict(value = {"projects", "project"}, allEntries = true)
    public ProjectResponse assignEmployee(String currentUserEmail, Long projectId, AssignProjectMemberRequest request) {
        User actor = getUserByEmail(currentUserEmail);
        if (actor.getRole() != Role.HR) {
            throw new ForbiddenOperationException("Only HR can assign employee to project");
        }

        Project project = getProject(projectId);
        if (project.getStatus() == ProjectStatus.ARCHIVED) {
            throw new ConflictException("Cannot assign employee to archived project");
        }

        User employee = userRepository.findByEmail(request.getEmployeeEmail().trim())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + request.getEmployeeEmail()));
        if (employee.getRole() == Role.ADMIN) {
            throw new BadRequestException("Cannot assign project to ADMIN");
        }

        ensureMember(project, employee, ProjectMemberRole.VIEWER);
        log(project, actor, "ASSIGN_EMPLOYEE", "Assigned employee: " + employee.getEmail());
        return ProjectMapper.toResponse(project);
    }

    private void ensureMember(Project project, User user, ProjectMemberRole role) {
        boolean exists = projectMemberRepository.existsByProject_IdAndUser_Id(project.getId(), user.getId());
        if (exists) return;
        projectMemberRepository.save(
                ProjectMember.builder()
                        .project(project)
                        .user(user)
                        .roleInProject(role)
                        .build()
        );
    }

    private void ensureValidStatusTransition(ProjectStatus current, ProjectStatus target) {
        if (current == target) return;
        if (current == ProjectStatus.PLANNING &&
                (target == ProjectStatus.ACTIVE || target == ProjectStatus.ON_HOLD || target == ProjectStatus.ARCHIVED)) {
            return;
        }
        if (current == ProjectStatus.ACTIVE &&
                (target == ProjectStatus.ON_HOLD || target == ProjectStatus.COMPLETED || target == ProjectStatus.ARCHIVED)) {
            return;
        }
        if (current == ProjectStatus.ON_HOLD &&
                (target == ProjectStatus.ACTIVE || target == ProjectStatus.ARCHIVED)) {
            return;
        }
        if (current == ProjectStatus.COMPLETED && target == ProjectStatus.ARCHIVED) {
            return;
        }
        throw new ConflictException("Invalid project status transition");
    }

    private boolean canView(User actor, Project project, Set<Long> memberProjectIds) {
        return canView(actor, project, memberProjectIds.contains(project.getId()));
    }

    private boolean canView(User actor, Project project, boolean isMember) {
        if (actor.getRole() == Role.HR) return true;
        if (actor.getRole() == Role.EMPLOYEE) return isMember;
        if (isMember) return true;
        if (project.getOwnerManager() != null && Objects.equals(project.getOwnerManager().getId(), actor.getId())) return true;
        ProjectVisibility visibility = project.getVisibility() != null ? project.getVisibility() : ProjectVisibility.DEPARTMENT;
        if (visibility == ProjectVisibility.COMPANY) return true;
        if (visibility == ProjectVisibility.DEPARTMENT) {
            return actor.getDepartment() != null && project.getDepartment() != null
                    && Objects.equals(actor.getDepartment().getId(), project.getDepartment().getId());
        }
        return false;
    }

    private void validateUniqueNameInDepartment(Long departmentId, String name, Long excludeProjectId) {
        if (departmentId == null || name == null || name.isBlank()) return;
        boolean exists = excludeProjectId == null
                ? projectRepository.existsByDepartment_IdAndNameIgnoreCase(departmentId, name.trim())
                : projectRepository.existsByDepartment_IdAndNameIgnoreCaseAndIdNot(departmentId, name.trim(), excludeProjectId);
        if (exists) {
            throw new ConflictException("Project name already exists in your department");
        }
    }

    private String generateProjectCode() {
        String code;
        do {
            code = "PRJ-" + LocalDateTime.now().getYear() + "-" + System.currentTimeMillis();
        } while (projectRepository.existsByProjectCode(code));
        return code;
    }

    private void ensureOwnerManager(User actor, Project project) {
        if (project.getOwnerManager() == null || !Objects.equals(project.getOwnerManager().getId(), actor.getId())) {
            throw new ForbiddenOperationException("Only project owner manager can modify this project");
        }
    }

    private void ensureManager(User actor) {
        if (actor.getRole() != Role.MANAGER) {
            throw new ForbiddenOperationException("Only manager can manage projects");
        }
    }

    private void ensureNonAdmin(User actor) {
        if (actor.getRole() == Role.ADMIN) {
            throw new ForbiddenOperationException("Admin cannot access project module");
        }
    }

    private Project getProject(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private void log(Project project, User actor, String action, String metadata) {
        auditLogRepository.save(AuditLog.builder()
                .entityType("PROJECT")
                .entityId(project.getId())
                .action(action)
                .actor(actor)
                .metadataJson(metadata)
                .build());
    }
}
