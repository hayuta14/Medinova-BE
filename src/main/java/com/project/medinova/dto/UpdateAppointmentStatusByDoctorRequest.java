package com.project.medinova.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Request to update appointment status by doctor")
public class UpdateAppointmentStatusByDoctorRequest {
    
    @NotBlank(message = "Status is required")
    @Schema(description = "New status for the appointment. Doctors can set status to CONFIRMED, CHECKED_IN, IN_PROGRESS, REVIEW, COMPLETED, CANCELLED, NO_SHOW, REJECTED, or CANCELLED_BY_DOCTOR.", 
            example = "CONFIRMED", 
            allowableValues = {"PENDING", "CONFIRMED", "CHECKED_IN", "IN_PROGRESS", "REVIEW", "COMPLETED", "CANCELLED", "NO_SHOW", "REJECTED", "EXPIRED", "CANCELLED_BY_DOCTOR", "CANCELLED_BY_PATIENT"})
    private String status;
}

