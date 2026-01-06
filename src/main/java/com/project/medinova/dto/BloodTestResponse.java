package com.project.medinova.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BloodTestResponse {
    private Long id;
    private Long patientId;
    private String patientName;
    private Long clinicId;
    private String clinicName;
    private String testType;
    private LocalDateTime testDate;
    private String testTime;
    private String status;
    private String resultFileUrl;
    private String notes;
    private Double price;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}




