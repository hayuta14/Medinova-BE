package com.project.medinova.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request to confirm an appointment and optionally update patient information")
public class ConfirmAppointmentRequest {
    
    @Schema(description = "Patient's age", example = "35")
    private Integer age;

    @Schema(description = "Patient's gender", example = "MALE", allowableValues = {"MALE", "FEMALE", "OTHER"})
    private String gender;

    @Schema(description = "Patient's symptoms or reason for appointment", example = "Headache and fever for 3 days")
    private String symptoms;
}


