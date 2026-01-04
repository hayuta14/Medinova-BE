package com.project.medinova.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateClinicRequest {
    @NotBlank(message = "Clinic name is required")
    @Size(max = 255, message = "Clinic name must not exceed 255 characters")
    private String name;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;

    private String description;

    @Schema(description = "Latitude of the clinic location", example = "10.762622")
    private Double latitude;

    @Schema(description = "Longitude of the clinic location", example = "106.660172")
    private Double longitude;

    @Schema(description = "Whether the clinic is active", example = "true")
    private Boolean isActive;

    @Schema(description = "Whether the clinic can handle emergency requests", example = "true")
    private Boolean emergencyEnabled;
}

