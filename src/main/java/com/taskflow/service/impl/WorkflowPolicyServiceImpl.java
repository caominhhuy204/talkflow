package com.taskflow.service.impl;

import com.taskflow.dto.request.UpdateWorkflowPolicyRequest;
import com.taskflow.dto.response.WorkflowPolicyResponse;
import com.taskflow.entity.WorkflowPolicy;
import com.taskflow.repository.WorkflowPolicyRepository;
import com.taskflow.service.WorkflowPolicyService;
import com.taskflow.workflow.DefaultWorkflowEngine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class WorkflowPolicyServiceImpl implements WorkflowPolicyService {

    private final WorkflowPolicyRepository workflowPolicyRepository;

    public WorkflowPolicyServiceImpl(WorkflowPolicyRepository workflowPolicyRepository) {
        this.workflowPolicyRepository = workflowPolicyRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public WorkflowPolicyResponse getPolicy() {
        BigDecimal threshold = workflowPolicyRepository.findById(DefaultWorkflowEngine.EQUIPMENT_ADMIN_THRESHOLD_KEY)
                .map(policy -> new BigDecimal(policy.getPolicyValue()))
                .orElse(new BigDecimal("5000"));
        return WorkflowPolicyResponse.builder()
                .equipmentAdminThreshold(threshold)
                .build();
    }

    @Override
    @Transactional
    public WorkflowPolicyResponse updatePolicy(UpdateWorkflowPolicyRequest request) {
        workflowPolicyRepository.save(WorkflowPolicy.builder()
                .policyKey(DefaultWorkflowEngine.EQUIPMENT_ADMIN_THRESHOLD_KEY)
                .policyValue(request.getEquipmentAdminThreshold().toPlainString())
                .build());
        return WorkflowPolicyResponse.builder()
                .equipmentAdminThreshold(request.getEquipmentAdminThreshold())
                .build();
    }
}
