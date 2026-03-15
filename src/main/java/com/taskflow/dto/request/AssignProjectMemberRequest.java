package com.taskflow.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignProjectMemberRequest {

    @NotBlank(message = "Employee email must not be blank")
    @Email(message = "Employee email is invalid")
    private String employeeEmail;
}
