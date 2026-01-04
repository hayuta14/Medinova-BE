package com.project.medinova.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request to update appointment consultation notes")
public class UpdateAppointmentNotesRequest {
    
    @Schema(description = "Doctor's consultation notes, diagnosis, and treatment plan", example = "Patient shows symptoms of common cold. Prescribed rest and medication.")
    private String notes;
}

