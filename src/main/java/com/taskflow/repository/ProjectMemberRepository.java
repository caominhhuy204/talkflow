package com.taskflow.repository;

import com.taskflow.entity.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {
    boolean existsByProject_IdAndUser_Id(Long projectId, Long userId);
    List<ProjectMember> findByUser_Id(Long userId);
}
