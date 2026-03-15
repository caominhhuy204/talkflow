package com.taskflow.repository;

import com.taskflow.entity.RequestCommentEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RequestCommentEntryRepository extends JpaRepository<RequestCommentEntry, Long> {
    List<RequestCommentEntry> findByRequest_IdOrderByCreatedAtAsc(Long requestId);
}
