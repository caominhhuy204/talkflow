package com.taskflow.workflow;

import com.taskflow.entity.InternalRequest;
import com.taskflow.entity.Role;

import java.util.List;

public interface WorkflowEngine {
    List<Role> resolveApproverRoles(InternalRequest request);
}
