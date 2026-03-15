package com.taskflow.service.impl;

import com.taskflow.dto.response.ReportSummaryResponse;
import com.taskflow.entity.ApprovalStep;
import com.taskflow.entity.ApprovalStepStatus;
import com.taskflow.entity.RequestStatus;
import com.taskflow.entity.RequestType;
import com.taskflow.entity.Role;
import com.taskflow.entity.User;
import com.taskflow.entity.InternalRequest;
import com.taskflow.entity.Project;
import com.taskflow.entity.ProjectStatus;
import com.taskflow.entity.ProjectVisibility;
import com.taskflow.exception.BadRequestException;
import com.taskflow.repository.ApprovalStepRepository;
import com.taskflow.repository.InternalRequestRepository;
import com.taskflow.repository.ProjectMemberRepository;
import com.taskflow.repository.ProjectRepository;
import com.taskflow.repository.UserRepository;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.service.ReportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    private final InternalRequestRepository internalRequestRepository;
    private final UserRepository userRepository;
    private final ApprovalStepRepository approvalStepRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public ReportServiceImpl(InternalRequestRepository internalRequestRepository,
                             UserRepository userRepository,
                             ApprovalStepRepository approvalStepRepository,
                             ProjectRepository projectRepository,
                             ProjectMemberRepository projectMemberRepository) {
        this.internalRequestRepository = internalRequestRepository;
        this.userRepository = userRepository;
        this.approvalStepRepository = approvalStepRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public ReportSummaryResponse summary(String currentUserEmail, LocalDate fromDate, LocalDate toDate) {
        User actor = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserEmail))
                ;

        validateDateRange(fromDate, toDate);
        List<InternalRequest> requests = applyRequestDateFilter(resolveRequestsForReport(actor), fromDate, toDate);
        List<Project> projects = applyProjectDateFilter(resolveProjectsForReport(actor), fromDate, toDate);

        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (RequestStatus status : RequestStatus.values()) {
            long count = requests.stream()
                    .filter(request -> request.getStatus() == status)
                    .count();
            byStatus.put(status.name(), count);
        }

        Map<String, Long> byType = new LinkedHashMap<>();
        for (RequestType type : RequestType.values()) {
            long count = requests.stream()
                    .filter(request -> request.getType() == type)
                    .count();
            byType.put(type.name(), count);
        }

        Map<String, Long> byProjectStatus = new LinkedHashMap<>();
        for (ProjectStatus status : ProjectStatus.values()) {
            long count = projects.stream()
                    .filter(project -> project.getStatus() == status)
                    .count();
            byProjectStatus.put(status.name(), count);
        }

        return ReportSummaryResponse.builder()
                .totalRequests((long) requests.size())
                .byStatus(byStatus)
                .byType(byType)
                .totalProjects((long) projects.size())
                .byProjectStatus(byProjectStatus)
                .build();
    }

    private List<InternalRequest> resolveRequestsForReport(User actor) {
        if (actor.getRole() == Role.ADMIN) {
            return internalRequestRepository.findAll();
        }

        if (actor.getRole() == Role.MANAGER || actor.getRole() == Role.HR) {
            Set<Long> ids = new LinkedHashSet<>();

            internalRequestRepository.findByRequesterIdOrderByCreatedAtDesc(actor.getId())
                    .stream()
                    .map(InternalRequest::getId)
                    .forEach(ids::add);

            List<ApprovalStep> pendingSteps = approvalStepRepository
                    .findByApproverRoleAndStatusOrderByIdDesc(actor.getRole(), ApprovalStepStatus.PENDING);
            pendingSteps.stream()
                    .map(step -> step.getRequest().getId())
                    .forEach(ids::add);

            return findAllByIds(ids);
        }

        return internalRequestRepository.findByRequesterIdOrderByCreatedAtDesc(actor.getId());
    }

    private List<InternalRequest> findAllByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return internalRequestRepository.findAllById(ids);
    }

    private List<Project> resolveProjectsForReport(User actor) {
        if (actor.getRole() == Role.ADMIN || actor.getRole() == Role.HR) {
            return projectRepository.findAll();
        }

        Set<Long> memberProjectIds = projectMemberRepository.findByUser_Id(actor.getId())
                .stream()
                .map(member -> member.getProject().getId())
                .collect(Collectors.toSet());

        return projectRepository.findAll().stream()
                .filter(project -> canViewProject(actor, project, memberProjectIds.contains(project.getId())))
                .toList();
    }

    private boolean canViewProject(User actor, Project project, boolean isMember) {
        if (actor.getRole() == Role.HR) return true;
        if (actor.getRole() == Role.EMPLOYEE) return isMember;
        if (isMember) return true;

        if (project.getOwnerManager() != null && Objects.equals(project.getOwnerManager().getId(), actor.getId())) {
            return true;
        }

        ProjectVisibility visibility = project.getVisibility() != null
                ? project.getVisibility()
                : ProjectVisibility.DEPARTMENT;
        if (visibility == ProjectVisibility.COMPANY) return true;
        if (visibility == ProjectVisibility.DEPARTMENT) {
            return actor.getDepartment() != null
                    && project.getDepartment() != null
                    && Objects.equals(actor.getDepartment().getId(), project.getDepartment().getId());
        }
        return false;
    }

    private void validateDateRange(LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new BadRequestException("fromDate must be before or equal to toDate");
        }
    }

    private List<InternalRequest> applyRequestDateFilter(List<InternalRequest> source, LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null && toDate == null) {
            return source;
        }

        LocalDateTime from = fromDate != null ? fromDate.atStartOfDay() : null;
        LocalDateTime to = toDate != null ? toDate.plusDays(1).atStartOfDay() : null;

        return source.stream()
                .filter(request -> isWithinRange(request.getCreatedAt(), from, to))
                .toList();
    }

    private List<Project> applyProjectDateFilter(List<Project> source, LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null && toDate == null) {
            return source;
        }

        LocalDateTime from = fromDate != null ? fromDate.atStartOfDay() : null;
        LocalDateTime to = toDate != null ? toDate.plusDays(1).atStartOfDay() : null;

        return source.stream()
                .filter(project -> isWithinRange(project.getCreatedAt(), from, to))
                .toList();
    }

    private boolean isWithinRange(LocalDateTime value, LocalDateTime fromInclusive, LocalDateTime toExclusive) {
        if (value == null) return false;
        if (fromInclusive != null && value.isBefore(fromInclusive)) return false;
        return toExclusive == null || value.isBefore(toExclusive);
    }
}
