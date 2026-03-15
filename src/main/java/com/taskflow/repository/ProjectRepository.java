package com.taskflow.repository;

import com.taskflow.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    boolean existsByProjectCode(String projectCode);
    boolean existsByDepartment_IdAndNameIgnoreCase(Long departmentId, String name);
    boolean existsByDepartment_IdAndNameIgnoreCaseAndIdNot(Long departmentId, String name, Long id);
}
