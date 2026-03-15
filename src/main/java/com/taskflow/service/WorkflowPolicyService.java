package com.taskflow.service;

import com.taskflow.dto.request.UpdateWorkflowPolicyRequest;
import com.taskflow.dto.response.WorkflowPolicyResponse;

public interface WorkflowPolicyService {
    WorkflowPolicyResponse getPolicy();
    WorkflowPolicyResponse updatePolicy(UpdateWorkflowPolicyRequest request);
}
