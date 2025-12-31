package com.project.medinova.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "Request to create a new appointment with a doctor")
public class CreateAppointmentRequest {
    
    @NotNull(message = "Doctor ID is required")
    @Positive(message = "Doctor ID must be positive")
    @Schema(description = "ID of the doctor for the appointment", example = "1")
    private Long doctorId;
    
    @NotNull(message = "Clinic ID is required")
    @Positive(message = "Clinic ID must be positive")
    @Schema(description = "ID of the clinic for the appointment", example = "1")
    private Long clinicId;
    
    @NotNull(message = "Appointment time is required")
    @Future(message = "Appointment time must be in the future")
    @Schema(description = "Date and time of the appointment", example = "2025-02-15T10:00:00")
    private LocalDateTime appointmentTime;
    
    @Schema(description = "Duration of the appointment in minutes (default: 60)", example = "60")
    private Integer durationMinutes = 60;

    @Schema(description = "Patient's age", example = "35")
    private Integer age;

    @Schema(description = "Patient's gender", example = "MALE", allowableValues = {"MALE", "FEMALE", "OTHER"})
    private String gender;

    @Schema(description = "Patient's symptoms or reason for appointment", example = "Headache and fever for 3 days")
    private String symptoms;
}

