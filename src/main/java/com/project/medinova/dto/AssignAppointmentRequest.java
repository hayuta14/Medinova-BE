package com.project.medinova.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Request to assign appointment to another doctor")
public class AssignAppointmentRequest {
    
    @NotNull(message = "Doctor ID is required")
    @Schema(description = "ID of the doctor to assign the appointment to", example = "2")
    private Long doctorId;
    
    @Schema(description = "Reason for assignment (optional)", example = "Doctor is on leave")
    private String reason;
}


