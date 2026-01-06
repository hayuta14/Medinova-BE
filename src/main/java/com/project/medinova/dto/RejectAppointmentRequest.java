package com.project.medinova.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request to reject an appointment by doctor")
public class RejectAppointmentRequest {
    
    @Schema(description = "Reason for rejection (internal, only visible to doctor and admin). Patient will see a generic message.", 
            example = "Doctor unavailable due to emergency")
    private String reason;
}




