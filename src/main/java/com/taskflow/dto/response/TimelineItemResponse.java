package com.taskflow.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimelineItemResponse {
    private String eventType;
    private String actor;
    private String action;
    private String content;
    private LocalDateTime occurredAt;
}
