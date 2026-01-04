package com.project.medinova.controller;

import com.project.medinova.dto.AppointmentResponse;
import com.project.medinova.dto.BusyScheduleResponse;
import com.project.medinova.dto.ConfirmAppointmentRequest;
import com.project.medinova.dto.CreateAppointmentRequest;
import com.project.medinova.dto.UpdateAppointmentStatusRequest;
import com.project.medinova.dto.UpdateAppointmentNotesRequest;
import com.project.medinova.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@CrossOrigin(origins = "*")
@Tag(name = "Appointment Management", description = "Appointment booking and management APIs")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @Operation(
            summary = "Create appointment (Hold slot)",
            description = "Create a new appointment with a doctor. This will hold the slot for 5 minutes. Only patients can create appointments. The system will automatically create a doctor schedule with HOLD status (1-1 relationship) for this appointment. The slot will be automatically released after 5 minutes if not confirmed. The system will validate that the doctor is not on leave, there are no conflicting appointments, and the doctor works at the specified clinic. After creating, use PUT /api/appointments/{id}/confirm to confirm the appointment within 5 minutes."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Appointment created successfully with a new doctor schedule"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad request - Doctor on leave, conflicting appointment, or doctor does not work at clinic"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Only patients can create appointments"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Doctor or clinic not found")
    })
    @PreAuthorize("hasRole('PATIENT')")
    @PostMapping
    public ResponseEntity<AppointmentResponse> createAppointment(@Valid @RequestBody CreateAppointmentRequest request) {
        AppointmentResponse appointment = appointmentService.createAppointment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(appointment);
    }

    @Operation(
            summary = "Get my appointments",
            description = "Get all appointments of the current authenticated user. Patients can see their own appointments, doctors can see appointments assigned to them. Can filter by status (PENDING, CONFIRMED, COMPLETED, CANCELLED)."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Appointments retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - This endpoint is for patients and doctors only"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Doctor profile not found (for doctors)")
    })
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/my-appointments")
    public ResponseEntity<List<AppointmentResponse>> getMyAppointments(
            @RequestParam(required = false) String status) {
        List<AppointmentResponse> appointments = appointmentService.getMyAppointments(status);
        return ResponseEntity.ok(appointments);
    }

    @Operation(
            summary = "Get today's appointments",
            description = "Get all appointments scheduled for today. Can filter by doctorId to get appointments for a specific doctor. CANCELLED appointments are excluded."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Today's appointments retrieved successfully")
    })
    @GetMapping("/today")
    public ResponseEntity<List<AppointmentResponse>> getTodayAppointments(
            @RequestParam(required = false) Long doctorId) {
        List<AppointmentResponse> appointments = appointmentService.getTodayAppointments(doctorId);
        return ResponseEntity.ok(appointments);
    }

    @Operation(
            summary = "Get all appointments (ADMIN only)",
            description = "Get all appointments with optional status filter and pagination. Only ADMIN can access all appointments. Orders appointments: future/current appointments (>= now) first in ascending order, then past appointments (< now) in descending order."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", 
                    description = "Appointments retrieved successfully",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Page.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad request - Invalid pagination parameters"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Only ADMIN can access all appointments")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<Page<AppointmentResponse>> getAllAppointments(
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {
        
        // Validate pagination parameters
        if (page < 0) page = 0;
        if (size < 1) size = 10;
        if (size > 100) size = 100; // Limit max page size
        
        Pageable pageable = PageRequest.of(page, size);
        Page<AppointmentResponse> appointments = appointmentService.getAllAppointments(status, pageable);
        
        return ResponseEntity.ok(appointments);
    }

    @Operation(
            summary = "Get doctor appointments with paging",
            description = "Get all appointments for a specific doctor with pagination. Orders appointments: future/current appointments (>= now) first in ascending order, then past appointments (< now) in descending order. Can filter by status (PENDING, CONFIRMED, COMPLETED, CANCELLED). Returns paginated results with appointment details."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", 
                    description = "Doctor appointments retrieved successfully",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Page.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad request - Invalid pagination parameters"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Doctor not found")
    })
    @GetMapping
    public ResponseEntity<Page<AppointmentResponse>> getAppointments(
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {
        
        if (doctorId == null) {
            // Nếu không có doctorId, trả về empty page
            Pageable pageable = PageRequest.of(page, size);
            return ResponseEntity.ok(new PageImpl<>(List.of(), pageable, 0));
        }
        
        // Validate pagination parameters
        if (page < 0) page = 0;
        if (size < 1) size = 10;
        if (size > 100) size = 100; // Limit max page size
        
        Pageable pageable = PageRequest.of(page, size);
        Page<AppointmentResponse> appointments = appointmentService.getDoctorAppointmentsWithPaging(doctorId, status, pageable);
        
        return ResponseEntity.ok(appointments);
    }

    @Operation(
            summary = "Get busy schedules for a doctor",
            description = "Get all busy schedules for a doctor, including appointments, HOLD slots (temporarily locked slots), and approved leave requests. HOLD slots are slots that are being held for 5 minutes and will be automatically released if not confirmed. This endpoint is public and can be used to check doctor availability."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Busy schedules retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Doctor not found")
    })
    @GetMapping("/doctors/{doctorId}/busy-schedules")
    public ResponseEntity<List<BusyScheduleResponse>> getBusySchedules(@PathVariable Long doctorId) {
        List<BusyScheduleResponse> busySchedules = appointmentService.getBusySchedules(doctorId);
        return ResponseEntity.ok(busySchedules);
    }

    @Operation(
            summary = "Update appointment status (Patient)",
            description = "Update the status of an appointment. Patients can only cancel their own appointments. The appointment must not be completed or already cancelled. When cancelled, the schedule slot will be made available again if the appointment time has not passed."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Appointment status updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad request - Cannot update completed/cancelled appointment or invalid status"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Can only update your own appointments"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Appointment not found")
    })
    @PreAuthorize("hasRole('PATIENT')")
    @PutMapping("/{id}/status")
    public ResponseEntity<AppointmentResponse> updateAppointmentStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAppointmentStatusRequest request) {
        AppointmentResponse appointment = appointmentService.updateAppointmentStatus(id, request);
        return ResponseEntity.ok(appointment);
    }

    @Operation(
            summary = "Update appointment status (Doctor)",
            description = "Update the status of an appointment assigned to the current doctor. Doctors can mark appointments as COMPLETED or update other statuses. Only appointments assigned to the current doctor can be updated."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Appointment status updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad request - Invalid status or appointment not assigned to doctor"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Can only update appointments assigned to you"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Appointment not found")
    })
    @PreAuthorize("hasRole('DOCTOR')")
    @PutMapping("/{id}/status/doctor")
    public ResponseEntity<AppointmentResponse> updateAppointmentStatusByDoctor(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAppointmentStatusRequest request) {
        AppointmentResponse appointment = appointmentService.updateAppointmentStatusByDoctor(id, request);
        return ResponseEntity.ok(appointment);
    }

    @Operation(
            summary = "Confirm appointment",
            description = "Confirm a pending appointment and optionally update patient information (age, gender, symptoms). This will convert the HOLD slot to BOOKED. The appointment must be in PENDING status with a HOLD schedule that has not expired (within 5 minutes)."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Appointment confirmed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad request - Hold period expired or appointment not in valid state"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Can only confirm your own appointments"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Appointment not found")
    })
    @PreAuthorize("hasRole('PATIENT')")
    @PutMapping("/{id}/confirm")
    public ResponseEntity<AppointmentResponse> confirmAppointment(
            @PathVariable Long id,
            @RequestBody(required = false) ConfirmAppointmentRequest request) {
        AppointmentResponse appointment = appointmentService.confirmAppointment(id, request);
        return ResponseEntity.ok(appointment);
    }

    @Operation(
            summary = "Update appointment consultation notes (Doctor)",
            description = "Update consultation notes, diagnosis, and treatment plan for an appointment. Only doctors assigned to the appointment can update notes."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Appointment notes updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Can only update notes for appointments assigned to you"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Appointment not found")
    })
    @PreAuthorize("hasRole('DOCTOR')")
    @PutMapping("/{id}/notes")
    public ResponseEntity<AppointmentResponse> updateAppointmentNotes(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAppointmentNotesRequest request) {
        AppointmentResponse appointment = appointmentService.updateAppointmentNotes(id, request);
        return ResponseEntity.ok(appointment);
    }
}

