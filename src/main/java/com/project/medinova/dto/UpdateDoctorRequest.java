package com.project.medinova.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for updating doctor information. All fields are optional.")
public class UpdateDoctorRequest {
    @Positive(message = "Clinic ID must be positive")
    @Schema(description = "ID of the clinic where the doctor works", example = "1")
    private Long clinicId;

    @Size(max = 255, message = "Specialization must not exceed 255 characters")
    @Schema(description = "Doctor's medical specialization", example = "Neurology")
    private String specialization;

    @Positive(message = "Experience years must be positive")
    @Schema(description = "Years of professional experience", example = "15")
    private Integer experienceYears;

    @Schema(description = "Doctor's biography and professional background", example = "Updated biography")
    private String bio;

    @Schema(description = "Default start time for doctor's working hours", example = "08:00:00")
    private LocalTime defaultStartTime;

    @Schema(description = "Default end time for doctor's working hours", example = "16:00:00")
    private LocalTime defaultEndTime;
}

