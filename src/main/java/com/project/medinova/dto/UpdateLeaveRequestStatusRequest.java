package com.project.medinova.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "Request to update leave request status")
public class UpdateLeaveRequestStatusRequest {
    
    @NotBlank(message = "Status is required")
    @Pattern(regexp = "APPROVED|REJECTED", message = "Status must be either APPROVED or REJECTED")
    @Schema(description = "New status for the leave request", example = "APPROVED", allowableValues = {"APPROVED", "REJECTED"})
    private String status;
}

