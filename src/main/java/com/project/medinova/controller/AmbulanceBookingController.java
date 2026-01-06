package com.project.medinova.controller;

import com.project.medinova.dto.AmbulanceBookingResponse;
import com.project.medinova.dto.CreateAmbulanceBookingRequest;
import com.project.medinova.service.AmbulanceBookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
@RequestMapping("/api/ambulance-bookings")
@CrossOrigin(origins = "*")
@Tag(name = "Ambulance Booking Management", description = "Ambulance booking and tracking APIs")
public class AmbulanceBookingController {

    @Autowired
    private AmbulanceBookingService ambulanceBookingService;

    @Operation(
            summary = "Create ambulance booking",
            description = "Create a new ambulance booking request. System will automatically find the nearest available ambulance. All authenticated users can create bookings."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Ambulance booking created successfully",
                    content = @Content(schema = @Schema(implementation = AmbulanceBookingResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad request - Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Clinic not found")
    })
    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<AmbulanceBookingResponse> createAmbulanceBooking(@Valid @RequestBody CreateAmbulanceBookingRequest request) {
        AmbulanceBookingResponse response = ambulanceBookingService.createAmbulanceBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Get ambulance booking by ID",
            description = "Get ambulance booking information by ID including tracking details. Patients can only view their own bookings, ADMIN and DOCTOR can view all."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Ambulance booking retrieved successfully",
                    content = @Content(schema = @Schema(implementation = AmbulanceBookingResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Cannot view other patients' bookings"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Ambulance booking not found")
    })
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<AmbulanceBookingResponse> getAmbulanceBookingById(@PathVariable Long id) {
        AmbulanceBookingResponse response = ambulanceBookingService.getAmbulanceBookingById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get my ambulance bookings",
            description = "Get all ambulance bookings created by the current user."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Ambulance bookings retrieved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/my-bookings")
    public ResponseEntity<List<AmbulanceBookingResponse>> getMyAmbulanceBookings() {
        List<AmbulanceBookingResponse> bookings = ambulanceBookingService.getMyAmbulanceBookings();
        return ResponseEntity.ok(bookings);
    }

    @Operation(
            summary = "Get all ambulance bookings",
            description = "Get all ambulance bookings with optional status filter. Only ADMIN and DOCTOR can access."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Ambulance bookings retrieved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Only ADMIN and DOCTOR can access")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @GetMapping
    public ResponseEntity<List<AmbulanceBookingResponse>> getAllAmbulanceBookings(
            @RequestParam(required = false) String status) {
        List<AmbulanceBookingResponse> bookings = ambulanceBookingService.getAllAmbulanceBookings(status);
        return ResponseEntity.ok(bookings);
    }

    @Operation(
            summary = "Get ambulance bookings by ambulance",
            description = "Get all bookings for a specific ambulance with optional status filter. Only ADMIN and DOCTOR can access."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Ambulance bookings retrieved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Only ADMIN and DOCTOR can access"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Ambulance not found")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @GetMapping("/ambulances/{ambulanceId}")
    public ResponseEntity<List<AmbulanceBookingResponse>> getAmbulanceBookingsByAmbulance(
            @PathVariable Long ambulanceId,
            @RequestParam(required = false) String status) {
        List<AmbulanceBookingResponse> bookings = ambulanceBookingService.getAmbulanceBookingsByAmbulance(ambulanceId, status);
        return ResponseEntity.ok(bookings);
    }

    @Operation(
            summary = "Update ambulance booking status",
            description = "Update ambulance booking status (PENDING, ASSIGNED, IN_TRANSIT, ARRIVED, COMPLETED, CANCELLED). Only ADMIN and DOCTOR can update."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Status updated successfully",
                    content = @Content(schema = @Schema(implementation = AmbulanceBookingResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad request - Invalid status"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Only ADMIN and DOCTOR can update"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Ambulance booking not found")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @PutMapping("/{id}/status")
    public ResponseEntity<AmbulanceBookingResponse> updateAmbulanceBookingStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        AmbulanceBookingResponse response = ambulanceBookingService.updateAmbulanceBookingStatus(id, status);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Assign ambulance to booking",
            description = "Manually assign an ambulance to a booking. Only ADMIN and DOCTOR can assign."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Ambulance assigned successfully",
                    content = @Content(schema = @Schema(implementation = AmbulanceBookingResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad request - Ambulance not available"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Only ADMIN and DOCTOR can assign"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Booking or ambulance not found")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @PutMapping("/{id}/assign-ambulance")
    public ResponseEntity<AmbulanceBookingResponse> assignAmbulance(
            @PathVariable Long id,
            @RequestParam Long ambulanceId) {
        AmbulanceBookingResponse response = ambulanceBookingService.assignAmbulance(id, ambulanceId);
        return ResponseEntity.ok(response);
    }
}




