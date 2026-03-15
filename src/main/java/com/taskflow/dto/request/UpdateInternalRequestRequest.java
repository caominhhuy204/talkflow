package com.taskflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateInternalRequestRequest {

    @NotBlank(message = "title is required")
    @Size(max = 200)
    private String title;
}
