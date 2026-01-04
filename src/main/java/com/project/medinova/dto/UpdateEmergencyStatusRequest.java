package com.project.medinova.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update emergency status")
public class UpdateEmergencyStatusRequest {
    
    @NotBlank(message = "Status is required")
    @Pattern(regexp = "PENDING|DISPATCHED|IN_TRANSIT|ARRIVED|COMPLETED|CANCELLED", 
            message = "Status must be one of: PENDING, DISPATCHED, IN_TRANSIT, ARRIVED, COMPLETED, CANCELLED")
    @Schema(description = "New status for the emergency", example = "DISPATCHED", 
            allowableValues = {"PENDING", "DISPATCHED", "IN_TRANSIT", "ARRIVED", "COMPLETED", "CANCELLED"})
    private String status;
}

