package com.taskflow.workflow;

import com.taskflow.entity.InternalRequest;
import com.taskflow.entity.RequestEquipment;
import com.taskflow.entity.RequestType;
import com.taskflow.entity.Role;
import com.taskflow.repository.RequestEquipmentRepository;
import com.taskflow.repository.WorkflowPolicyRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class DefaultWorkflowEngine implements WorkflowEngine {

    public static final String EQUIPMENT_ADMIN_THRESHOLD_KEY = "EQUIPMENT_ADMIN_THRESHOLD";

    private final RequestEquipmentRepository requestEquipmentRepository;
    private final WorkflowPolicyRepository workflowPolicyRepository;

    public DefaultWorkflowEngine(RequestEquipmentRepository requestEquipmentRepository,
                                 WorkflowPolicyRepository workflowPolicyRepository) {
        this.requestEquipmentRepository = requestEquipmentRepository;
        this.workflowPolicyRepository = workflowPolicyRepository;
    }

    @Override
    public List<Role> resolveApproverRoles(InternalRequest request) {
        List<Role> roles = new ArrayList<>();
        RequestType type = request.getType();

        if (type == RequestType.LEAVE) {
            roles.add(Role.MANAGER);
            roles.add(Role.HR);
            return roles;
        }

        if (type == RequestType.EXPENSE_REIMBURSEMENT || type == RequestType.OVERTIME) {
            roles.add(Role.MANAGER);
            roles.add(Role.HR);
            return roles;
        }

        if (type == RequestType.EQUIPMENT_PURCHASE) {
            roles.add(Role.MANAGER);
            roles.add(Role.HR);
            RequestEquipment equipment = requestEquipmentRepository.findById(request.getId()).orElse(null);
            if (equipment != null && equipment.getEstimatedCost() != null) {
                BigDecimal threshold = workflowPolicyRepository.findById(EQUIPMENT_ADMIN_THRESHOLD_KEY)
                        .map(policy -> new BigDecimal(policy.getPolicyValue()))
                        .orElse(new BigDecimal("5000"));
                if (equipment.getEstimatedCost().compareTo(threshold) > 0) {
                    roles.add(Role.ADMIN);
                }
            }
            return roles;
        }

        roles.add(Role.MANAGER);
        roles.add(Role.HR);
        return roles;
    }
}
