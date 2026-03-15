package com.taskflow.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {
    private String code;
    private String message;
    private int status;
    private LocalDateTime timestamp;
    private String traceId;
    private Map<String, String> errors;
}
