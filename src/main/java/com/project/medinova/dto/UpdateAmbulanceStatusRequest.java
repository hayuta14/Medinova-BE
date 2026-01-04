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
@Schema(description = "Request to update ambulance status")
public class UpdateAmbulanceStatusRequest {
    
    @NotBlank(message = "Status is required")
    @Pattern(regexp = "AVAILABLE|BUSY|MAINTENANCE|DISPATCHED", message = "Status must be AVAILABLE, BUSY, MAINTENANCE, or DISPATCHED")
    @Schema(description = "Status", example = "AVAILABLE", allowableValues = {"AVAILABLE", "BUSY", "MAINTENANCE", "DISPATCHED"})
    private String status;
}


