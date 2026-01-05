package com.project.medinova.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateSurgeryConsultationRequest {
    @NotNull(message = "Clinic ID is required")
    private Long clinicId;

    @NotBlank(message = "Surgery type is required")
    private String surgeryType;

    @NotBlank(message = "Description is required")
    private String description;

    private String urgency; // ROUTINE | URGENT | EMERGENCY
}



