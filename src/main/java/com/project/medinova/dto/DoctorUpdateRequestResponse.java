package com.project.medinova.dto;

import com.project.medinova.entity.Department;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorUpdateRequestResponse {
    private Long id;
    private Long doctorId;
    private String doctorName;
    private String doctorEmail;
    private Long clinicId;
    private String clinicName;
    private Department department;
    private String departmentDisplayName;
    private Integer experienceYears;
    private String bio;
    private LocalTime defaultStartTime;
    private LocalTime defaultEndTime;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;
    
    // Current profile values for comparison
    private Department currentDepartment;
    private String currentDepartmentDisplayName;
    private Integer currentExperienceYears;
    private String currentBio;
    private LocalTime currentDefaultStartTime;
    private LocalTime currentDefaultEndTime;
}



