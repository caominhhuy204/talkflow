package com.taskflow.controller;

import com.taskflow.dto.request.ApprovalActionRequest;
import com.taskflow.dto.response.ApiResponse;
import com.taskflow.dto.response.ApprovalInboxItemResponse;
import com.taskflow.dto.response.InternalRequestResponse;
import com.taskflow.service.ApprovalService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/approvals")
public class ApprovalController {

    private final ApprovalService approvalService;

    public ApprovalController(ApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    @GetMapping("/inbox")
    @PreAuthorize("hasAnyRole('MANAGER','HR')")
    public ResponseEntity<ApiResponse<List<ApprovalInboxItemResponse>>> inbox(Principal principal) {
        return ResponseEntity.ok(
                ApiResponse.<List<ApprovalInboxItemResponse>>builder()
                        .message("Inbox fetched")
                        .data(approvalService.inbox(principal.getName()))
                        .build()
        );
    }

    @PostMapping("/{requestId}/approve")
    @PreAuthorize("hasAnyRole('MANAGER','HR')")
    public ResponseEntity<ApiResponse<InternalRequestResponse>> approve(Principal principal,
                                                                        @PathVariable Long requestId,
                                                                        @Valid @RequestBody ApprovalActionRequest request) {
        return ResponseEntity.ok(
                ApiResponse.<InternalRequestResponse>builder()
                        .message("Request approved")
                        .data(approvalService.approve(principal.getName(), requestId, request))
                        .build()
        );
    }

    @PostMapping("/{requestId}/reject")
    @PreAuthorize("hasAnyRole('MANAGER','HR')")
    public ResponseEntity<ApiResponse<InternalRequestResponse>> reject(Principal principal,
                                                                       @PathVariable Long requestId,
                                                                       @Valid @RequestBody ApprovalActionRequest request) {
        return ResponseEntity.ok(
                ApiResponse.<InternalRequestResponse>builder()
                        .message("Request rejected")
                        .data(approvalService.reject(principal.getName(), requestId, request))
                        .build()
        );
    }
}
