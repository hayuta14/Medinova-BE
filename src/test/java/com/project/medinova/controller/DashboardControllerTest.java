package com.project.medinova.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.medinova.dto.DashboardStatsResponse;
import com.project.medinova.dto.DoctorDashboardStatsResponse;
import com.project.medinova.exception.GlobalExceptionHandler;
import com.project.medinova.service.DashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    @Mock
    private DashboardService dashboardService;

    @InjectMocks
    private DashboardController dashboardController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(dashboardController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAdminDashboardStats_Success() throws Exception {
        DashboardStatsResponse stats = new DashboardStatsResponse();
        stats.setTotalHospitals(5L);
        stats.setTotalDoctors(20L);
        stats.setTotalPatients(100L);
        stats.setTotalUsers(125L);
        stats.setPendingDoctors(3L);
        stats.setPendingLeaveRequests(2L);
        stats.setTodayAppointments(10L);
        stats.setTodayEmergencies(2L);
        stats.setActiveEmergencies(5L);
        stats.setTotalAppointments(500L);
        stats.setTotalEmergencies(50L);

        when(dashboardService.getAdminDashboardStats()).thenReturn(stats);

        mockMvc.perform(get("/api/dashboard/admin")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalHospitals").value(5))
                .andExpect(jsonPath("$.totalDoctors").value(20))
                .andExpect(jsonPath("$.totalPatients").value(100));
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    void testGetAdminDashboardStats_Forbidden() throws Exception {
        mockMvc.perform(get("/api/dashboard/admin")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetAdminDashboardStats_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/dashboard/admin")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    void testGetDoctorDashboardStats_Success() throws Exception {
        DoctorDashboardStatsResponse stats = new DoctorDashboardStatsResponse();
        stats.setTotalAppointments(50L);
        stats.setTodayAppointments(5L);
        stats.setUpcomingAppointments(10L);
        stats.setCompletedAppointments(35L);
        stats.setTotalEmergencies(10L);
        stats.setActiveEmergencies(2L);
        stats.setTodayEmergencies(1L);
        stats.setPendingLeaveRequests(1L);

        when(dashboardService.getDoctorDashboardStats()).thenReturn(stats);

        mockMvc.perform(get("/api/dashboard/doctor")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAppointments").value(50))
                .andExpect(jsonPath("$.todayAppointments").value(5))
                .andExpect(jsonPath("$.upcomingAppointments").value(10));
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    void testGetDoctorDashboardStats_Forbidden() throws Exception {
        mockMvc.perform(get("/api/dashboard/doctor")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetDoctorDashboardStats_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/dashboard/doctor")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}

