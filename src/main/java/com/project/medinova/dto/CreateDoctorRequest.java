package com.project.medinova.dto;

import com.project.medinova.entity.Department;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for creating a new doctor profile")
public class CreateDoctorRequest {
    @NotNull(message = "User ID is required")
    @Positive(message = "User ID must be positive")
    @Schema(description = "ID of the user who will become a doctor. User must have DOCTOR role.", example = "2")
    private Long userId;

    @NotNull(message = "Clinic ID is required")
    @Positive(message = "Clinic ID must be positive")
    @Schema(description = "ID of the clinic where the doctor will work", example = "1")
    private Long clinicId;

    @NotNull(message = "Department is required")
    @Schema(description = "Doctor's medical department/specialty", example = "CARDIOLOGY", 
            allowableValues = {"GENERAL_MEDICINE", "PEDIATRICS", "OBSTETRICS_GYNECOLOGY", "SURGERY", 
                              "CARDIOLOGY", "NEUROLOGY", "ORTHOPEDICS", "ONCOLOGY", "GASTROENTEROLOGY", 
                              "RESPIRATORY", "NEPHROLOGY", "ENDOCRINOLOGY", "HEMATOLOGY", "RHEUMATOLOGY", 
                              "DERMATOLOGY", "INFECTIOUS_DISEASE"})
    private Department department;

    @Positive(message = "Experience years must be positive")
    @Schema(description = "Years of professional experience", example = "10")
    private Integer experienceYears;

    @Schema(description = "Doctor's biography and professional background", example = "Experienced cardiologist with expertise in heart diseases")
    private String bio;

    @Schema(description = "Default start time for doctor's working hours", example = "09:00:00")
    private LocalTime defaultStartTime;

    @Schema(description = "Default end time for doctor's working hours", example = "17:00:00")
    private LocalTime defaultEndTime;
}

