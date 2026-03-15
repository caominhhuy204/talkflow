package com.taskflow.dto.response;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportSummaryResponse {
    private long totalRequests;
    private Map<String, Long> byStatus;
    private Map<String, Long> byType;
    private long totalProjects;
    private Map<String, Long> byProjectStatus;
}
