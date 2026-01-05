package com.project.medinova.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorRankingItem {
    private Long id;
    private String name;
    private String specialization;
    private String clinicName;
    private Double averageRating;
    private Long totalReviews;
    private Long totalAppointments;
    private Integer experienceYears;
    private Integer rank;
}



