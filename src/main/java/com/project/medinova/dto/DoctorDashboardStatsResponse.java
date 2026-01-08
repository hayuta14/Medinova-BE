package com.project.medinova.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorDashboardStatsResponse {
    private Long totalAppointments;
    private Long todayAppointments;
    private Long upcomingAppointments;
    private Long completedAppointments;
    private Long totalEmergencies;
    private Long activeEmergencies;
    private Long todayEmergencies;
    private Long pendingLeaveRequests;
    private Map<String, Long> appointmentsByStatus;
    private Map<String, Long> emergenciesByStatus;
}






