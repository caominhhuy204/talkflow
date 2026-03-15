package com.taskflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAttachmentRequest {
    @NotBlank
    @Size(max = 1000)
    private String fileUrl;

    @NotBlank
    @Size(max = 255)
    private String fileName;
}
