package com.taskflow.service;

import com.taskflow.dto.response.ReportSummaryResponse;

import java.time.LocalDate;

public interface ReportService {
    ReportSummaryResponse summary(String currentUserEmail, LocalDate fromDate, LocalDate toDate);
}
