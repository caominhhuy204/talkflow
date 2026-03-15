package com.taskflow.service.impl;

import com.taskflow.dto.request.ApprovalActionRequest;
import com.taskflow.dto.response.ApprovalInboxItemResponse;
import com.taskflow.dto.response.InternalRequestResponse;
import com.taskflow.entity.*;
import com.taskflow.exception.ConflictException;
import com.taskflow.exception.ForbiddenOperationException;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.repository.*;
import com.taskflow.service.ApprovalService;
import com.taskflow.util.InternalRequestMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ApprovalServiceImpl implements ApprovalService {

    private final ApprovalStepRepository approvalStepRepository;
    private final InternalRequestRepository internalRequestRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;

    public ApprovalServiceImpl(ApprovalStepRepository approvalStepRepository,
                               InternalRequestRepository internalRequestRepository,
                               UserRepository userRepository,
                               AuditLogRepository auditLogRepository) {
        this.approvalStepRepository = approvalStepRepository;
        this.internalRequestRepository = internalRequestRepository;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApprovalInboxItemResponse> inbox(String currentUserEmail) {
        User actor = getUserByEmail(currentUserEmail);
        ensureCanApprove(actor);
        return approvalStepRepository.findByApproverRoleAndStatusOrderByIdDesc(
                        actor.getRole(),
                        ApprovalStepStatus.PENDING
                )
                .stream()
                .filter(step -> !step.getRequest().getRequester().getId().equals(actor.getId()))
                .map(InternalRequestMapper::toInbox)
                .toList();
    }

    @Override
    @Transactional
    public InternalRequestResponse approve(String currentUserEmail, Long requestId, ApprovalActionRequest request) {
        return applyDecision(currentUserEmail, requestId, request, true);
    }

    @Override
    @Transactional
    public InternalRequestResponse reject(String currentUserEmail, Long requestId, ApprovalActionRequest request) {
        return applyDecision(currentUserEmail, requestId, request, false);
    }

    private InternalRequestResponse applyDecision(String email, Long requestId, ApprovalActionRequest request, boolean approved) {
        User actor = getUserByEmail(email);
        ensureCanApprove(actor);
        InternalRequest target = internalRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with id: " + requestId));
        if (target.getStatus() != RequestStatus.IN_REVIEW) {
            throw new ConflictException("Request is not in review state");
        }
        if (target.getRequester().getId().equals(actor.getId())) {
            throw new ForbiddenOperationException("Requester cannot self-approve");
        }

        ApprovalStep currentStep = approvalStepRepository.findByRequest_IdAndStepNo(requestId, target.getCurrentStep())
                .orElseThrow(() -> new ResourceNotFoundException("Current approval step not found"));
        if (currentStep.getApproverRole() != actor.getRole()) {
            throw new ForbiddenOperationException("Current user role cannot act on this step");
        }
        if (currentStep.getStatus() != ApprovalStepStatus.PENDING) {
            throw new ConflictException("Current approval step is not pending");
        }

        currentStep.setApprover(actor);
        currentStep.setActedAt(LocalDateTime.now());
        currentStep.setComment(request.getComment());
        currentStep.setAction(approved ? ApprovalAction.APPROVE : ApprovalAction.REJECT);
        currentStep.setStatus(approved ? ApprovalStepStatus.APPROVED : ApprovalStepStatus.REJECTED);
        approvalStepRepository.save(currentStep);

        if (!approved) {
            target.setStatus(RequestStatus.REJECTED);
            target.setFinalDecisionAt(LocalDateTime.now());
            internalRequestRepository.save(target);
            log(target, actor, "REJECT", request.getComment());
            return InternalRequestMapper.toResponse(target);
        }

        ApprovalStep nextStep = approvalStepRepository.findByRequest_IdAndStepNo(requestId, target.getCurrentStep() + 1)
                .orElse(null);
        if (nextStep == null) {
            target.setStatus(RequestStatus.APPROVED);
            target.setFinalDecisionAt(LocalDateTime.now());
        } else {
            target.setCurrentStep(target.getCurrentStep() + 1);
            nextStep.setStatus(ApprovalStepStatus.PENDING);
            approvalStepRepository.save(nextStep);
        }
        internalRequestRepository.save(target);
        log(target, actor, "APPROVE", request.getComment());
        return InternalRequestMapper.toResponse(target);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private void log(InternalRequest request, User actor, String action, String metadata) {
        auditLogRepository.save(AuditLog.builder()
                .entityType("REQUEST")
                .entityId(request.getId())
                .action(action)
                .actor(actor)
                .metadataJson(metadata)
                .build());
    }

    private void ensureCanApprove(User actor) {
        if (actor.getRole() != Role.MANAGER && actor.getRole() != Role.HR) {
            throw new ForbiddenOperationException("Current role cannot approve requests");
        }
    }
}
