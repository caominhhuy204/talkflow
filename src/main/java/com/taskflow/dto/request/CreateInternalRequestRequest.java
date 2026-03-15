package com.taskflow.dto.request;

import com.taskflow.entity.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateInternalRequestRequest {

    @NotNull(message = "type is required")
    private RequestType type;

    @NotBlank(message = "title is required")
    @Size(max = 200)
    private String title;

    @Valid
    private LeavePayload leave;

    @Valid
    private ExpensePayload expense;

    @Valid
    private OvertimePayload overtime;

    @Valid
    private EquipmentPayload equipment;

    @Valid
    private DocumentPayload document;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LeavePayload {
        @NotNull
        private LeaveType leaveType;
        @NotNull
        private LocalDate startDate;
        @NotNull
        private LocalDate endDate;
        @NotBlank
        @Size(max = 1000)
        private String reason;
        @Size(max = 1000)
        private String handoverNote;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExpensePayload {
        @NotNull
        @Positive
        private BigDecimal amount;
        @NotNull
        private CurrencyCode currency;
        @NotNull
        private LocalDate expenseDate;
        @NotNull
        private ExpenseCategory category;
        @NotBlank
        @Size(max = 1000)
        private String description;
        private List<@NotBlank String> receiptUrls;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OvertimePayload {
        @NotNull
        private LocalDate date;
        @NotNull
        private LocalTime startTime;
        @NotNull
        private LocalTime endTime;
        @NotNull
        @Positive
        private BigDecimal totalHours;
        @NotBlank
        @Size(max = 1000)
        private String reason;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EquipmentPayload {
        @NotBlank
        @Size(max = 200)
        private String itemName;
        @NotNull
        @Min(1)
        private Integer quantity;
        @NotNull
        @Positive
        private BigDecimal estimatedCost;
        @NotBlank
        @Size(max = 1000)
        private String businessJustification;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DocumentPayload {
        @NotNull
        private DocumentType documentType;
        @NotBlank
        @Size(max = 200)
        private String title;
        @NotBlank
        @Size(max = 1000)
        private String contentUrl;
        @NotBlank
        @Size(max = 30)
        private String version;
    }
}
