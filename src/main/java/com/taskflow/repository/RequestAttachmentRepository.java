package com.taskflow.repository;

import com.taskflow.entity.RequestAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RequestAttachmentRepository extends JpaRepository<RequestAttachment, Long> {
    List<RequestAttachment> findByRequest_IdOrderByUploadedAtAsc(Long requestId);
}
