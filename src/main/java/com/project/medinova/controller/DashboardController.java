package com.project.medinova.controller;

import com.project.medinova.dto.DashboardStatsResponse;
import com.project.medinova.dto.DoctorDashboardStatsResponse;
import com.project.medinova.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
@Tag(name = "Dashboard", description = "Dashboard statistics APIs")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Operation(
            summary = "Get admin dashboard stats",
            description = "Get comprehensive statistics for admin dashboard including total hospitals, doctors, patients, appointments, emergencies, and status breakdowns. (ADMIN only)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Dashboard stats retrieved successfully",
                    content = @Content(schema = @Schema(implementation = DashboardStatsResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Only ADMIN can access")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public ResponseEntity<DashboardStatsResponse> getAdminDashboardStats() {
        DashboardStatsResponse stats = dashboardService.getAdminDashboardStats();
        return ResponseEntity.ok(stats);
    }

    @Operation(
            summary = "Get doctor dashboard stats",
            description = "Get statistics for doctor dashboard including appointments, emergencies, and leave requests. (DOCTOR only)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Dashboard stats retrieved successfully",
                    content = @Content(schema = @Schema(implementation = DoctorDashboardStatsResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Only DOCTOR can access"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Doctor profile not found")
    })
    @PreAuthorize("hasRole('DOCTOR')")
    @GetMapping("/doctor")
    public ResponseEntity<DoctorDashboardStatsResponse> getDoctorDashboardStats() {
        DoctorDashboardStatsResponse stats = dashboardService.getDoctorDashboardStats();
        return ResponseEntity.ok(stats);
    }
}

