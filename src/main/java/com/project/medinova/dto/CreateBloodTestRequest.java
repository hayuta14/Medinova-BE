package com.project.medinova.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateBloodTestRequest {
    @NotNull(message = "Clinic ID is required")
    private Long clinicId;

    @NotBlank(message = "Test type is required")
    private String testType;

    @NotNull(message = "Test date is required")
    private LocalDate testDate;

    @NotBlank(message = "Test time is required")
    private String testTime; // HH:mm format

    private String notes;
}



