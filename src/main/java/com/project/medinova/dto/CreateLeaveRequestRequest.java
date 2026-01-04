package com.project.medinova.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Schema(description = "Request to create a leave request")
public class CreateLeaveRequestRequest {
    
    @NotNull(message = "Start date is required")
    @FutureOrPresent(message = "Start date must be today or in the future")
    @Schema(description = "Start date of the leave", example = "2025-01-15")
    private LocalDate startDate;
    
    @NotNull(message = "End date is required")
    @FutureOrPresent(message = "End date must be today or in the future")
    @Schema(description = "End date of the leave", example = "2025-01-20")
    private LocalDate endDate;
    
    @Schema(description = "Start time of the leave (optional, for partial day leave)", example = "09:00:00")
    private LocalTime startTime;
    
    @Schema(description = "End time of the leave (optional, for partial day leave)", example = "17:00:00")
    private LocalTime endTime;
    
    @Schema(description = "Reason for the leave request", example = "Family emergency")
    private String reason;
}

