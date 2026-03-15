package com.taskflow.util;

import com.taskflow.dto.response.ApprovalInboxItemResponse;
import com.taskflow.dto.response.InternalRequestResponse;
import com.taskflow.entity.ApprovalStep;
import com.taskflow.entity.InternalRequest;

public final class InternalRequestMapper {
    private InternalRequestMapper() {
    }

    public static InternalRequestResponse toResponse(InternalRequest entity) {
        return InternalRequestResponse.builder()
                .id(entity.getId())
                .requestCode(entity.getRequestCode())
                .type(entity.getType())
                .status(entity.getStatus())
                .title(entity.getTitle())
                .currentStep(entity.getCurrentStep())
                .requesterId(entity.getRequester().getId())
                .requesterName(entity.getRequester().getFullName())
                .requesterEmail(entity.getRequester().getEmail())
                .submittedAt(entity.getSubmittedAt())
                .finalDecisionAt(entity.getFinalDecisionAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public static ApprovalInboxItemResponse toInbox(ApprovalStep step) {
        InternalRequest request = step.getRequest();
        return ApprovalInboxItemResponse.builder()
                .requestId(request.getId())
                .requestCode(request.getRequestCode())
                .title(request.getTitle())
                .type(request.getType())
                .stepNo(step.getStepNo())
                .approverRole(step.getApproverRole().name())
                .requesterName(request.getRequester().getFullName())
                .requesterEmail(request.getRequester().getEmail())
                .submittedAt(request.getSubmittedAt())
                .build();
    }
}
