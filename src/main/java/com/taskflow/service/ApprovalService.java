package com.taskflow.service;

import com.taskflow.dto.request.ApprovalActionRequest;
import com.taskflow.dto.response.ApprovalInboxItemResponse;
import com.taskflow.dto.response.InternalRequestResponse;

import java.util.List;

public interface ApprovalService {
    List<ApprovalInboxItemResponse> inbox(String currentUserEmail);
    InternalRequestResponse approve(String currentUserEmail, Long requestId, ApprovalActionRequest request);
    InternalRequestResponse reject(String currentUserEmail, Long requestId, ApprovalActionRequest request);
}
