package com.project.medinova.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response containing busy schedule information for a doctor")
public class BusyScheduleResponse {
    
    @Schema(description = "Type of busy schedule: APPOINTMENT, HOLD, or LEAVE", example = "APPOINTMENT", allowableValues = {"APPOINTMENT", "HOLD", "LEAVE"})
    private String type; // APPOINTMENT | HOLD | LEAVE
    
    @Schema(description = "Start date/time of the busy period", example = "2025-02-15T10:00:00")
    private LocalDateTime startDateTime;
    
    @Schema(description = "End date/time of the busy period", example = "2025-02-15T11:00:00")
    private LocalDateTime endDateTime;
    
    @Schema(description = "Start date (for leave requests)", example = "2025-02-15")
    private LocalDate startDate;
    
    @Schema(description = "End date (for leave requests)", example = "2025-02-20")
    private LocalDate endDate;
    
    @Schema(description = "Reason or description", example = "Regular checkup")
    private String reason;
    
    @Schema(description = "Appointment ID (if type is APPOINTMENT)", example = "1")
    private Long appointmentId;
    
    @Schema(description = "Leave request ID (if type is LEAVE)", example = "1")
    private Long leaveRequestId;
}

