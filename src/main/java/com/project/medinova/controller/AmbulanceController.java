package com.project.medinova.controller;

import com.project.medinova.dto.*;
import com.project.medinova.service.AmbulanceService;
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
@RequestMapping("/api/ambulances")
@CrossOrigin(origins = "*")
@Tag(name = "Ambulance Management", description = "Ambulance CRUD and management APIs")
public class AmbulanceController {

    @Autowired
    private AmbulanceService ambulanceService;

    @Operation(
            summary = "Create ambulance",
            description = "Create a new ambulance (ADMIN only)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Ambulance created successfully",
                    content = @Content(schema = @Schema(implementation = AmbulanceResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad request - Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Only ADMIN can create ambulance"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Clinic not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<AmbulanceResponse> createAmbulance(@Valid @RequestBody CreateAmbulanceRequest request) {
        AmbulanceResponse response = ambulanceService.createAmbulance(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Get ambulance by ID",
            description = "Get ambulance information by ID"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Ambulance retrieved successfully",
                    content = @Content(schema = @Schema(implementation = AmbulanceResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Ambulance not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<AmbulanceResponse> getAmbulanceById(@PathVariable Long id) {
        AmbulanceResponse response = ambulanceService.getAmbulanceById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get all ambulances",
            description = "Get list of all ambulances. Can filter by clinicId and status using query parameters."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "List of ambulances retrieved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    public ResponseEntity<List<AmbulanceResponse>> getAllAmbulances(
            @RequestParam(required = false) Long clinicId,
            @RequestParam(required = false) String status) {
        
        List<AmbulanceResponse> responses;
        
        if (clinicId != null && status != null) {
            // Filter by both clinicId and status
            responses = ambulanceService.getAmbulancesByClinicId(clinicId).stream()
                    .filter(amb -> status.equals(amb.getStatus()))
                    .toList();
        } else if (clinicId != null) {
            responses = ambulanceService.getAmbulancesByClinicId(clinicId);
        } else if (status != null) {
            responses = ambulanceService.getAmbulancesByStatus(status);
        } else {
            responses = ambulanceService.getAllAmbulances();
        }
        
        return ResponseEntity.ok(responses);
    }

    @Operation(
            summary = "Update ambulance",
            description = "Update ambulance information (ADMIN only)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Ambulance updated successfully",
                    content = @Content(schema = @Schema(implementation = AmbulanceResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad request - Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Only ADMIN can update ambulance"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Ambulance not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<AmbulanceResponse> updateAmbulance(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAmbulanceRequest request) {
        AmbulanceResponse response = ambulanceService.updateAmbulance(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Update ambulance location",
            description = "Update ambulance current location (latitude and longitude). Can be used by drivers or tracking systems."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Ambulance location updated successfully",
                    content = @Content(schema = @Schema(implementation = AmbulanceResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad request - Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Ambulance not found")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @PutMapping("/{id}/location")
    public ResponseEntity<AmbulanceResponse> updateAmbulanceLocation(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAmbulanceLocationRequest request) {
        AmbulanceResponse response = ambulanceService.updateAmbulanceLocation(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Update ambulance status",
            description = "Update ambulance status (ADMIN only)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Ambulance status updated successfully",
                    content = @Content(schema = @Schema(implementation = AmbulanceResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad request - Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Only ADMIN can update ambulance status"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Ambulance not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/status")
    public ResponseEntity<AmbulanceResponse> updateAmbulanceStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAmbulanceStatusRequest request) {
        AmbulanceResponse response = ambulanceService.updateAmbulanceStatus(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Delete ambulance",
            description = "Delete an ambulance (ADMIN only)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Ambulance deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Only ADMIN can delete ambulance"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Ambulance not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAmbulance(@PathVariable Long id) {
        ambulanceService.deleteAmbulance(id);
        return ResponseEntity.noContent().build();
    }
}


