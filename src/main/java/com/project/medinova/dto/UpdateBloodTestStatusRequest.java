package com.project.medinova.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateBloodTestStatusRequest {
    @NotBlank(message = "Status is required")
    private String status;
}






