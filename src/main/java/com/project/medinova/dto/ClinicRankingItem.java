package com.project.medinova.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClinicRankingItem {
    private Long id;
    private String name;
    private String address;
    private Long totalDoctors;
    private Long totalAppointments;
    private Double averageDoctorRating;
    private Integer rank;
}




