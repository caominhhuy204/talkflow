package com.taskflow.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalActionRequest {
    @Size(max = 1000)
    private String comment;
}
