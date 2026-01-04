package com.project.medinova.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create an emergency request")
public class CreateEmergencyRequest {
    
    @Schema(description = "ID of the clinic handling the emergency (optional - if not provided, system will find nearest clinic)", example = "1")
    private Long clinicId;
    
    @NotNull(message = "Patient latitude is required")
    @Schema(description = "Latitude of patient location", example = "10.762622")
    private Double patientLat;
    
    @NotNull(message = "Patient longitude is required")
    @Schema(description = "Longitude of patient location", example = "106.660172")
    private Double patientLng;
    
    @Schema(description = "Patient address", example = "123 Main Street, District 1")
    private String patientAddress;
    
    @Schema(description = "Patient name", example = "Nguyen Van A")
    private String patientName;
    
    @Schema(description = "Patient phone number", example = "0912345678")
    private String patientPhone;
    
    @Schema(description = "Emergency description", example = "Chest pain, difficulty breathing")
    private String description;
    
    @Schema(description = "Priority level", example = "HIGH", allowableValues = {"LOW", "MEDIUM", "HIGH", "CRITICAL"})
    private String priority;
}

