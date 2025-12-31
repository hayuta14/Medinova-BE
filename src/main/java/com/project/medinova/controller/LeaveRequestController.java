package com.project.medinova.controller;

import com.project.medinova.dto.CreateLeaveRequestRequest;
import com.project.medinova.dto.UpdateLeaveRequestStatusRequest;
import com.project.medinova.entity.DoctorLeaveRequest;
import com.project.medinova.service.LeaveRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leave-requests")
@CrossOrigin(origins = "*")
@Tag(name = "Leave Request Management", description = "Doctor leave request management APIs")
public class LeaveRequestController {

    @Autowired
    private LeaveRequestService leaveRequestService;

    @Operation(
            summary = "Create leave request",
            description = "Create a new leave request. Only doctors can create leave requests for themselves."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Leave request created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad request - Invalid dates or overlapping request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Only doctors can create leave requests"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Doctor profile not found")
    })
    @PreAuthorize("hasRole('DOCTOR')")
    @PostMapping
    public ResponseEntity<DoctorLeaveRequest> createLeaveRequest(@Valid @RequestBody CreateLeaveRequestRequest request) {
        DoctorLeaveRequest leaveRequest = leaveRequestService.createLeaveRequest(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(leaveRequest);
    }

    @Operation(
            summary = "Get leave request by ID",
            description = "Get leave request information by ID. Doctors can only view their own requests, admins can view all."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Leave request retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Cannot view other doctors' requests"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Leave request not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<DoctorLeaveRequest> getLeaveRequestById(@PathVariable Long id) {
        DoctorLeaveRequest leaveRequest = leaveRequestService.getLeaveRequestById(id);
        return ResponseEntity.ok(leaveRequest);
    }

    @Operation(
            summary = "Get my leave requests",
            description = "Get all leave requests created by the current doctor. Only doctors can access this endpoint."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Leave requests retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Only doctors can access this endpoint"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Doctor profile not found")
    })
    @PreAuthorize("hasRole('DOCTOR')")
    @GetMapping("/my-requests")
    public ResponseEntity<List<DoctorLeaveRequest>> getMyLeaveRequests() {
        List<DoctorLeaveRequest> leaveRequests = leaveRequestService.getMyLeaveRequests();
        return ResponseEntity.ok(leaveRequests);
    }

    @Operation(
            summary = "Get all leave requests",
            description = "Get all leave requests in the system. Only admins can access this endpoint."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Leave requests retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Only admins can access this endpoint")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<DoctorLeaveRequest>> getAllLeaveRequests() {
        List<DoctorLeaveRequest> leaveRequests = leaveRequestService.getAllLeaveRequests();
        return ResponseEntity.ok(leaveRequests);
    }

    @Operation(
            summary = "Update leave request status",
            description = "Update the status of a leave request (APPROVED or REJECTED). Only admins can update leave request status. Only pending requests can be updated."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Leave request status updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad request - Only pending requests can be updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Only admins can update leave request status"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Leave request not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/status")
    public ResponseEntity<DoctorLeaveRequest> updateLeaveRequestStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateLeaveRequestStatusRequest request) {
        DoctorLeaveRequest leaveRequest = leaveRequestService.updateLeaveRequestStatus(id, request);
        return ResponseEntity.ok(leaveRequest);
    }
}

