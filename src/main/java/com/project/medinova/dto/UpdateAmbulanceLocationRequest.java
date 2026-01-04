package com.project.medinova.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update ambulance location")
public class UpdateAmbulanceLocationRequest {
    
    @NotNull(message = "Latitude is required")
    @Schema(description = "Current latitude", example = "10.762622")
    private Double currentLat;
    
    @NotNull(message = "Longitude is required")
    @Schema(description = "Current longitude", example = "106.660172")
    private Double currentLng;
}


