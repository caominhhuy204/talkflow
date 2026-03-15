package com.taskflow.repository;

import com.taskflow.entity.InternalRequest;
import com.taskflow.entity.RequestStatus;
import com.taskflow.entity.RequestType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InternalRequestRepository extends JpaRepository<InternalRequest, Long> {
    List<InternalRequest> findByRequesterIdOrderByCreatedAtDesc(Long requesterId);
    long countByStatus(RequestStatus status);
    long countByType(RequestType type);
    long countByRequesterId(Long requesterId);
    long countByRequesterIdAndStatus(Long requesterId, RequestStatus status);
    long countByRequesterIdAndType(Long requesterId, RequestType type);
}
