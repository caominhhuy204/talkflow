package com.taskflow.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateWorkflowPolicyRequest {
    @NotNull
    @Positive
    private BigDecimal equipmentAdminThreshold;
}
