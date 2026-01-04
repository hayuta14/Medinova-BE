package com.project.medinova.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublicStatsResponse {
    private Long totalHospitals;
    private Long totalDoctors;
    private Long totalPatients;
    private Long totalAppointments;
    private List<DoctorSummary> featuredDoctors;
    private List<ClinicSummary> featuredClinics;
    private List<PostSummary> recentPosts;
}

