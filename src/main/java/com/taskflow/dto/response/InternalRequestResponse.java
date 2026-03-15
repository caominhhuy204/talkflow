package com.taskflow.dto.response;

import com.taskflow.entity.RequestStatus;
import com.taskflow.entity.RequestType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InternalRequestResponse {
    private Long id;
    private String requestCode;
    private RequestType type;
    private RequestStatus status;
    private String title;
    private Integer currentStep;
    private Long requesterId;
    private String requesterName;
    private String requesterEmail;
    private LocalDateTime submittedAt;
    private LocalDateTime finalDecisionAt;
    private LocalDateTime createdAt;
}
