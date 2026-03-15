package com.taskflow.dto.response;

import com.taskflow.entity.RequestType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalInboxItemResponse {
    private Long requestId;
    private String requestCode;
    private String title;
    private RequestType type;
    private Integer stepNo;
    private String approverRole;
    private String requesterName;
    private String requesterEmail;
    private LocalDateTime submittedAt;
}
