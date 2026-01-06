package com.project.medinova.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    private Long totalHospitals;
    private Long totalDoctors;
    private Long totalPatients;
    private Long totalUsers;
    private Long pendingDoctors;
    private Long pendingLeaveRequests;
    private Long todayAppointments;
    private Long todayEmergencies;
    private Long activeEmergencies;
    private Long totalAppointments;
    private Long totalEmergencies;
    private Map<String, Long> appointmentsByStatus;
    private Map<String, Long> emergenciesByStatus;
    private Map<String, Long> doctorsByStatus;
}




