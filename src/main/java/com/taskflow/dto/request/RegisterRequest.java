package com.taskflow.dto.request;

import com.taskflow.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

    @NotBlank(message = "Full name must not be blank")
    private String fullName;

    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email format is invalid")
    private String email;

    @NotBlank(message = "Password must not be blank")
    @Size(min = 6, max = 100, message = "Password length must be between 6 and 100")
    private String password;

    @NotNull(message = "Role must not be null")
    private Role role;
}