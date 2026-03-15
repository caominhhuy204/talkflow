package com.taskflow.controller;

import com.taskflow.dto.request.UpdateWorkflowPolicyRequest;
import com.taskflow.dto.response.ApiResponse;
import com.taskflow.dto.response.WorkflowPolicyResponse;
import com.taskflow.service.WorkflowPolicyService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workflow-policies")
public class WorkflowPolicyController {

    private final WorkflowPolicyService workflowPolicyService;

    public WorkflowPolicyController(WorkflowPolicyService workflowPolicyService) {
        this.workflowPolicyService = workflowPolicyService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<WorkflowPolicyResponse>> getPolicy() {
        return ResponseEntity.ok(
                ApiResponse.<WorkflowPolicyResponse>builder()
                        .message("Workflow policy fetched")
                        .data(workflowPolicyService.getPolicy())
                        .build()
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping
    public ResponseEntity<ApiResponse<WorkflowPolicyResponse>> updatePolicy(
            @Valid @RequestBody UpdateWorkflowPolicyRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.<WorkflowPolicyResponse>builder()
                        .message("Workflow policy updated")
                        .data(workflowPolicyService.updatePolicy(request))
                        .build()
        );
    }
}
