package com.taskflow.repository;

import com.taskflow.entity.RequestLeave;
import com.taskflow.entity.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface RequestLeaveRepository extends JpaRepository<RequestLeave, Long> {

    @Query("""
            select case when count(rl) > 0 then true else false end
            from RequestLeave rl
            where rl.request.requester.id = :requesterId
              and rl.request.status = :status
              and rl.startDate <= :endDate
              and rl.endDate >= :startDate
              and (:excludeRequestId is null or rl.request.id <> :excludeRequestId)
            """)
    boolean existsOverlapApproved(@Param("requesterId") Long requesterId,
                                  @Param("status") RequestStatus status,
                                  @Param("startDate") LocalDate startDate,
                                  @Param("endDate") LocalDate endDate,
                                  @Param("excludeRequestId") Long excludeRequestId);
}
