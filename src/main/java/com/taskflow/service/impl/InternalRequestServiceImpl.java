package com.taskflow.service.impl;

import com.taskflow.dto.request.CreateAttachmentRequest;
import com.taskflow.dto.request.CreateInternalRequestRequest;
import com.taskflow.dto.request.CreateRequestCommentRequest;
import com.taskflow.dto.request.UpdateInternalRequestRequest;
import com.taskflow.dto.response.InternalRequestResponse;
import com.taskflow.dto.response.TimelineItemResponse;
import com.taskflow.entity.*;
import com.taskflow.exception.BadRequestException;
import com.taskflow.exception.ConflictException;
import com.taskflow.exception.ForbiddenOperationException;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.repository.*;
import com.taskflow.service.InternalRequestService;
import com.taskflow.util.InternalRequestMapper;
import com.taskflow.workflow.WorkflowEngine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class InternalRequestServiceImpl implements InternalRequestService {

    private final InternalRequestRepository internalRequestRepository;
    private final RequestLeaveRepository requestLeaveRepository;
    private final RequestExpenseRepository requestExpenseRepository;
    private final RequestOvertimeRepository requestOvertimeRepository;
    private final RequestEquipmentRepository requestEquipmentRepository;
    private final RequestDocumentRepository requestDocumentRepository;
    private final ApprovalStepRepository approvalStepRepository;
    private final RequestCommentEntryRepository requestCommentEntryRepository;
    private final RequestAttachmentRepository requestAttachmentRepository;
    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final WorkflowEngine workflowEngine;

    public InternalRequestServiceImpl(InternalRequestRepository internalRequestRepository,
                                      RequestLeaveRepository requestLeaveRepository,
                                      RequestExpenseRepository requestExpenseRepository,
                                      RequestOvertimeRepository requestOvertimeRepository,
                                      RequestEquipmentRepository requestEquipmentRepository,
                                      RequestDocumentRepository requestDocumentRepository,
                                      ApprovalStepRepository approvalStepRepository,
                                      RequestCommentEntryRepository requestCommentEntryRepository,
                                      RequestAttachmentRepository requestAttachmentRepository,
                                      AuditLogRepository auditLogRepository,
                                      UserRepository userRepository,
                                      WorkflowEngine workflowEngine) {
        this.internalRequestRepository = internalRequestRepository;
        this.requestLeaveRepository = requestLeaveRepository;
        this.requestExpenseRepository = requestExpenseRepository;
        this.requestOvertimeRepository = requestOvertimeRepository;
        this.requestEquipmentRepository = requestEquipmentRepository;
        this.requestDocumentRepository = requestDocumentRepository;
        this.approvalStepRepository = approvalStepRepository;
        this.requestCommentEntryRepository = requestCommentEntryRepository;
        this.requestAttachmentRepository = requestAttachmentRepository;
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
        this.workflowEngine = workflowEngine;
    }

    @Override
    @Transactional
    public InternalRequestResponse create(String currentUserEmail, CreateInternalRequestRequest request) {
        User requester = getUserByEmail(currentUserEmail);
        ensureCanUseInternalRequestModule(requester);
        validatePayloadByType(request, null, requester);

        InternalRequest entity = InternalRequest.builder()
                .requestCode(generateCode(request.getType()))
                .type(request.getType())
                .status(RequestStatus.DRAFT)
                .requester(requester)
                .department(requester.getDepartment())
                .title(request.getTitle())
                .currentStep(0)
                .build();
        InternalRequest saved = internalRequestRepository.save(entity);

        saveDetail(saved, request);
        log(saved, requester, "CREATE_DRAFT", "Request draft created");
        return InternalRequestMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InternalRequestResponse> list(String currentUserEmail) {
        User user = getUserByEmail(currentUserEmail);
        ensureCanUseInternalRequestModule(user);
        List<InternalRequest> data = internalRequestRepository.findByRequesterIdOrderByCreatedAtDesc(user.getId());
        return data.stream().map(InternalRequestMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public InternalRequestResponse getById(String currentUserEmail, Long id) {
        User user = getUserByEmail(currentUserEmail);
        InternalRequest entity = getRequest(id);
        ensureCanView(user, entity);
        return InternalRequestMapper.toResponse(entity);
    }

    @Override
    @Transactional
    public InternalRequestResponse update(String currentUserEmail, Long id, UpdateInternalRequestRequest request) {
        User user = getUserByEmail(currentUserEmail);
        ensureCanUseInternalRequestModule(user);
        InternalRequest entity = getRequest(id);
        ensureRequester(user, entity);
        if (entity.getStatus() != RequestStatus.DRAFT) {
            throw new ConflictException("Only DRAFT request can be updated");
        }
        entity.setTitle(request.getTitle());
        internalRequestRepository.save(entity);
        log(entity, user, "UPDATE_DRAFT", "Request draft updated");
        return InternalRequestMapper.toResponse(entity);
    }

    @Override
    @Transactional
    public InternalRequestResponse submit(String currentUserEmail, Long id) {
        User user = getUserByEmail(currentUserEmail);
        ensureCanUseInternalRequestModule(user);
        InternalRequest entity = getRequest(id);
        ensureRequester(user, entity);
        if (entity.getStatus() != RequestStatus.DRAFT) {
            throw new ConflictException("Only DRAFT request can be submitted");
        }

        entity.setStatus(RequestStatus.IN_REVIEW);
        entity.setSubmittedAt(LocalDateTime.now());
        entity.setCurrentStep(1);
        internalRequestRepository.save(entity);

        List<Role> roles = workflowEngine.resolveApproverRoles(entity);
        for (int i = 0; i < roles.size(); i++) {
            approvalStepRepository.save(ApprovalStep.builder()
                    .request(entity)
                    .stepNo(i + 1)
                    .approverRole(roles.get(i))
                    .status(i == 0 ? ApprovalStepStatus.PENDING : ApprovalStepStatus.WAITING)
                    .build());
        }

        log(entity, user, "SUBMIT", "Request submitted for approval");
        return InternalRequestMapper.toResponse(entity);
    }

    @Override
    @Transactional
    public InternalRequestResponse cancel(String currentUserEmail, Long id) {
        User user = getUserByEmail(currentUserEmail);
        ensureCanUseInternalRequestModule(user);
        InternalRequest entity = getRequest(id);
        ensureRequester(user, entity);
        if (entity.getStatus() == RequestStatus.APPROVED || entity.getStatus() == RequestStatus.REJECTED) {
            throw new ConflictException("Finalized request cannot be cancelled");
        }
        entity.setStatus(RequestStatus.CANCELLED);
        entity.setFinalDecisionAt(LocalDateTime.now());
        internalRequestRepository.save(entity);
        log(entity, user, "CANCEL", "Request cancelled");
        return InternalRequestMapper.toResponse(entity);
    }

    @Override
    @Transactional
    public TimelineItemResponse addComment(String currentUserEmail, Long id, CreateRequestCommentRequest request) {
        User user = getUserByEmail(currentUserEmail);
        InternalRequest entity = getRequest(id);
        ensureCanView(user, entity);
        RequestCommentEntry saved = requestCommentEntryRepository.save(
                RequestCommentEntry.builder()
                        .request(entity)
                        .author(user)
                        .content(request.getContent())
                        .build()
        );
        log(entity, user, "COMMENT", "Added comment");
        return TimelineItemResponse.builder()
                .eventType("COMMENT")
                .actor(user.getEmail())
                .action("COMMENT")
                .content(saved.getContent())
                .occurredAt(saved.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public TimelineItemResponse addAttachment(String currentUserEmail, Long id, CreateAttachmentRequest request) {
        User user = getUserByEmail(currentUserEmail);
        InternalRequest entity = getRequest(id);
        ensureCanView(user, entity);
        RequestAttachment saved = requestAttachmentRepository.save(
                RequestAttachment.builder()
                        .request(entity)
                        .uploadedBy(user)
                        .fileName(request.getFileName())
                        .fileUrl(request.getFileUrl())
                        .build()
        );
        log(entity, user, "ATTACHMENT", "Added attachment");
        return TimelineItemResponse.builder()
                .eventType("ATTACHMENT")
                .actor(user.getEmail())
                .action("UPLOAD")
                .content(saved.getFileName())
                .occurredAt(saved.getUploadedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimelineItemResponse> timeline(String currentUserEmail, Long id) {
        User user = getUserByEmail(currentUserEmail);
        InternalRequest entity = getRequest(id);
        ensureCanView(user, entity);

        List<TimelineItemResponse> items = new ArrayList<>();
        approvalStepRepository.findByRequest_IdOrderByStepNoAsc(id).forEach(step -> {
            if (step.getActedAt() != null) {
                items.add(TimelineItemResponse.builder()
                        .eventType("APPROVAL_STEP")
                        .actor(step.getApprover() != null ? step.getApprover().getEmail() : step.getApproverRole().name())
                        .action(step.getAction() != null ? step.getAction().name() : step.getStatus().name())
                        .content(step.getComment())
                        .occurredAt(step.getActedAt())
                        .build());
            }
        });
        requestCommentEntryRepository.findByRequest_IdOrderByCreatedAtAsc(id).forEach(comment -> {
            items.add(TimelineItemResponse.builder()
                    .eventType("COMMENT")
                    .actor(comment.getAuthor().getEmail())
                    .action("COMMENT")
                    .content(comment.getContent())
                    .occurredAt(comment.getCreatedAt())
                    .build());
        });
        auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtAsc("REQUEST", id).forEach(log -> {
            items.add(TimelineItemResponse.builder()
                    .eventType("AUDIT")
                    .actor(log.getActor() != null ? log.getActor().getEmail() : "SYSTEM")
                    .action(log.getAction())
                    .content(log.getMetadataJson())
                    .occurredAt(log.getCreatedAt())
                    .build());
        });
        items.sort(Comparator.comparing(TimelineItemResponse::getOccurredAt));
        return items;
    }

    private void validatePayloadByType(CreateInternalRequestRequest request, Long excludeRequestId, User requester) {
        if (request.getType() == RequestType.LEAVE) {
            if (request.getLeave() == null) {
                throw new BadRequestException("leave payload is required");
            }
            if (request.getLeave().getEndDate().isBefore(request.getLeave().getStartDate())) {
                throw new BadRequestException("leave end_date must be >= start_date");
            }
            boolean overlap = requestLeaveRepository.existsOverlapApproved(
                    requester.getId(),
                    RequestStatus.APPROVED,
                    request.getLeave().getStartDate(),
                    request.getLeave().getEndDate(),
                    excludeRequestId
            );
            if (overlap) {
                throw new ConflictException("Leave period overlaps an approved leave");
            }
            return;
        }
        if (request.getType() == RequestType.EXPENSE_REIMBURSEMENT && request.getExpense() == null) {
            throw new BadRequestException("expense payload is required");
        }
        if (request.getType() == RequestType.OVERTIME) {
            if (request.getOvertime() == null) {
                throw new BadRequestException("overtime payload is required");
            }
            if (!request.getOvertime().getEndTime().isAfter(request.getOvertime().getStartTime())) {
                throw new BadRequestException("overtime end_time must be after start_time");
            }
        }
        if (request.getType() == RequestType.EQUIPMENT_PURCHASE && request.getEquipment() == null) {
            throw new BadRequestException("equipment payload is required");
        }
        if (request.getType() == RequestType.DOCUMENT_APPROVAL && request.getDocument() == null) {
            throw new BadRequestException("document payload is required");
        }
    }

    private void saveDetail(InternalRequest request, CreateInternalRequestRequest payload) {
        if (request.getType() == RequestType.LEAVE) {
            requestLeaveRepository.save(
                    RequestLeave.builder()
                            .request(request)
                            .leaveType(payload.getLeave().getLeaveType())
                            .startDate(payload.getLeave().getStartDate())
                            .endDate(payload.getLeave().getEndDate())
                            .reason(payload.getLeave().getReason())
                            .handoverNote(payload.getLeave().getHandoverNote())
                            .build()
            );
            return;
        }
        if (request.getType() == RequestType.EXPENSE_REIMBURSEMENT) {
            requestExpenseRepository.save(
                    RequestExpense.builder()
                            .request(request)
                            .amount(payload.getExpense().getAmount())
                            .currency(payload.getExpense().getCurrency())
                            .expenseDate(payload.getExpense().getExpenseDate())
                            .category(payload.getExpense().getCategory())
                            .description(payload.getExpense().getDescription())
                            .build()
            );
            return;
        }
        if (request.getType() == RequestType.OVERTIME) {
            requestOvertimeRepository.save(
                    RequestOvertime.builder()
                            .request(request)
                            .date(payload.getOvertime().getDate())
                            .startTime(payload.getOvertime().getStartTime())
                            .endTime(payload.getOvertime().getEndTime())
                            .totalHours(payload.getOvertime().getTotalHours())
                            .reason(payload.getOvertime().getReason())
                            .build()
            );
            return;
        }
        if (request.getType() == RequestType.EQUIPMENT_PURCHASE) {
            requestEquipmentRepository.save(
                    RequestEquipment.builder()
                            .request(request)
                            .itemName(payload.getEquipment().getItemName())
                            .quantity(payload.getEquipment().getQuantity())
                            .estimatedCost(payload.getEquipment().getEstimatedCost())
                            .businessJustification(payload.getEquipment().getBusinessJustification())
                            .build()
            );
            return;
        }
        requestDocumentRepository.save(
                RequestDocument.builder()
                        .request(request)
                        .documentType(payload.getDocument().getDocumentType())
                        .title(payload.getDocument().getTitle())
                        .contentUrl(payload.getDocument().getContentUrl())
                        .version(payload.getDocument().getVersion())
                        .build()
        );
    }

    private void ensureRequester(User actor, InternalRequest request) {
        if (!request.getRequester().getId().equals(actor.getId())) {
            throw new ForbiddenOperationException("Only requester can perform this action");
        }
    }

    private void ensureCanView(User actor, InternalRequest request) {
        boolean isRequester = request.getRequester().getId().equals(actor.getId());
        if (isRequester) return;

        boolean isRelatedApprover = approvalStepRepository.existsByRequest_IdAndApproverRole(
                request.getId(),
                actor.getRole()
        );
        if (!isRelatedApprover) throw new ForbiddenOperationException("You cannot view this request");
    }

    private void ensureCanUseInternalRequestModule(User actor) {
        if (actor.getRole() == Role.ADMIN) {
            throw new ForbiddenOperationException("Admin cannot use internal request module");
        }
    }

    private String generateCode(RequestType type) {
        return type.name().replace('_', '-') + "-" + System.currentTimeMillis();
    }

    private InternalRequest getRequest(Long id) {
        return internalRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with id: " + id));
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
}
