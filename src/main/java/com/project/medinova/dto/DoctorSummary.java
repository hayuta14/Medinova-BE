package com.project.medinova.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorSummary {
    private Long id;
    private String name;
    private String specialization;
    private Integer experienceYears;
    private Double averageRating;
    private Long totalReviews;
    private String clinicName;
}

