package com.project.medinova.dto;

import com.project.medinova.entity.Department;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorSummary {
    private Long id;
    private String name;
    private Department department;
    private String departmentDisplayName;
    private Integer experienceYears;
    private Double averageRating;
    private Long totalReviews;
    private String clinicName;
}


