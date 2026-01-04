package com.project.medinova.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to manually assign doctor and ambulance to an emergency")
public class AssignEmergencyRequest {
    
    @NotNull(message = "Doctor ID is required")
    @Schema(description = "ID of the doctor to assign", example = "1")
    private Long doctorId;
    
    @Schema(description = "ID of the ambulance to assign (optional, can be null if no ambulance available)", example = "1")
    private Long ambulanceId;
}

