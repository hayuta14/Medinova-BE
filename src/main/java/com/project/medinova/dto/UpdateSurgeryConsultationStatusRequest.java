package com.project.medinova.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateSurgeryConsultationStatusRequest {
    @NotBlank(message = "Status is required")
    private String status;
}



