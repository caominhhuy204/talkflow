package com.taskflow.repository;

import com.taskflow.entity.WorkflowPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkflowPolicyRepository extends JpaRepository<WorkflowPolicy, String> {
}
