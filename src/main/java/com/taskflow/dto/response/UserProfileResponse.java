package com.taskflow.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileResponse {
    private Long id;
    private String fullName;
    private String email;
    private String role;
    private String departmentCode;
    private Long managerId;
}
