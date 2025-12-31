package com.project.medinova.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "Request to update appointment status")
public class UpdateAppointmentStatusRequest {
    
    @NotBlank(message = "Status is required")
    @Pattern(regexp = "CANCELLED", message = "Patients can only cancel appointments. Status must be CANCELLED")
    @Schema(description = "New status for the appointment. Patients can only set status to CANCELLED.", example = "CANCELLED", allowableValues = {"CANCELLED"})
    private String status;
}

