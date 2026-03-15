package com.taskflow.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProfileRequest {

    @Size(min = 2, max = 255, message = "Full name length must be between 2 and 255")
    private String fullName;

    @Size(min = 6, max = 100, message = "Password length must be between 6 and 100")
    private String newPassword;
}
