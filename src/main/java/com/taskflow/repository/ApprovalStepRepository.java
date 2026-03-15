package com.taskflow.repository;

import com.taskflow.entity.ApprovalStep;
import com.taskflow.entity.ApprovalStepStatus;
import com.taskflow.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApprovalStepRepository extends JpaRepository<ApprovalStep, Long> {
    List<ApprovalStep> findByRequest_IdOrderByStepNoAsc(Long requestId);
    Optional<ApprovalStep> findByRequest_IdAndStepNo(Long requestId, Integer stepNo);
    List<ApprovalStep> findByApproverRoleAndStatusOrderByIdDesc(Role approverRole, ApprovalStepStatus status);
    boolean existsByRequest_IdAndApproverRole(Long requestId, Role approverRole);
}
