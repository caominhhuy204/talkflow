package com.taskflow.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowPolicyResponse {
    private BigDecimal equipmentAdminThreshold;
}
