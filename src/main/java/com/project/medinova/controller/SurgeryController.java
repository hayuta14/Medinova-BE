package com.project.medinova.controller;

import com.project.medinova.dto.CreateSurgeryConsultationRequest;
import com.project.medinova.dto.SurgeryConsultationResponse;
import com.project.medinova.service.SurgeryService;
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
@RequestMapping("/api/surgery-consultations")
@CrossOrigin(origins = "*")
@Tag(name = "Surgery Consultation Management", description = "Surgery consultation request and management APIs")
public class SurgeryController {

    @Autowired
    private SurgeryService surgeryService;

    @Operation(
            summary = "Create surgery consultation request",
            description = "Create a new surgery consultation request. Only patients can create requests. Surgery cannot be booked directly - consultation is required first."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Surgery consultation request created successfully",
                    content = @Content(schema = @Schema(implementation = SurgeryConsultationResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad request - Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Only patients can create requests"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Clinic not found")
    })
    @PreAuthorize("hasRole('PATIENT')")
    @PostMapping
    public ResponseEntity<SurgeryConsultationResponse> createSurgeryConsultation(@Valid @RequestBody CreateSurgeryConsultationRequest request) {
        SurgeryConsultationResponse response = surgeryService.createSurgeryConsultation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Get surgery consultation by ID",
            description = "Get surgery consultation information by ID. Patients can only view their own consultations, ADMIN and DOCTOR can view all."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Surgery consultation retrieved successfully",
                    content = @Content(schema = @Schema(implementation = SurgeryConsultationResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Cannot view other patients' consultations"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Surgery consultation not found")
    })
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<SurgeryConsultationResponse> getSurgeryConsultationById(@PathVariable Long id) {
        SurgeryConsultationResponse response = surgeryService.getSurgeryConsultationById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get my surgery consultations",
            description = "Get all surgery consultations created by the current patient."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Surgery consultations retrieved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/my-consultations")
    public ResponseEntity<List<SurgeryConsultationResponse>> getMySurgeryConsultations() {
        List<SurgeryConsultationResponse> consultations = surgeryService.getMySurgeryConsultations();
        return ResponseEntity.ok(consultations);
    }

    @Operation(
            summary = "Get all surgery consultations",
            description = "Get all surgery consultations with optional status filter. Only ADMIN and DOCTOR can access."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Surgery consultations retrieved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Only ADMIN and DOCTOR can access")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @GetMapping
    public ResponseEntity<List<SurgeryConsultationResponse>> getAllSurgeryConsultations(
            @RequestParam(required = false) String status) {
        List<SurgeryConsultationResponse> consultations = surgeryService.getAllSurgeryConsultations(status);
        return ResponseEntity.ok(consultations);
    }

    @Operation(
            summary = "Get surgery consultations by doctor",
            description = "Get all surgery consultations assigned to a specific doctor with optional status filter. Only ADMIN and DOCTOR can access."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Surgery consultations retrieved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Only ADMIN and DOCTOR can access"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Doctor not found")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @GetMapping("/doctors/{doctorId}")
    public ResponseEntity<List<SurgeryConsultationResponse>> getSurgeryConsultationsByDoctor(
            @PathVariable Long doctorId,
            @RequestParam(required = false) String status) {
        List<SurgeryConsultationResponse> consultations = surgeryService.getSurgeryConsultationsByDoctor(doctorId, status);
        return ResponseEntity.ok(consultations);
    }

    @Operation(
            summary = "Assign doctor to consultation",
            description = "Assign a doctor to a surgery consultation. Only ADMIN and DOCTOR can assign."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Doctor assigned successfully",
                    content = @Content(schema = @Schema(implementation = SurgeryConsultationResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Only ADMIN and DOCTOR can assign"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Consultation or doctor not found")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @PutMapping("/{id}/assign-doctor")
    public ResponseEntity<SurgeryConsultationResponse> assignDoctor(
            @PathVariable Long id,
            @RequestParam Long doctorId) {
        SurgeryConsultationResponse response = surgeryService.assignDoctor(id, doctorId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Update surgery consultation status",
            description = "Update surgery consultation status. Only ADMIN and DOCTOR can update."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Status updated successfully",
                    content = @Content(schema = @Schema(implementation = SurgeryConsultationResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad request - Invalid status"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Only ADMIN and DOCTOR can update"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Surgery consultation not found")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @PutMapping("/{id}/status")
    public ResponseEntity<SurgeryConsultationResponse> updateSurgeryConsultationStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        SurgeryConsultationResponse response = surgeryService.updateSurgeryConsultationStatus(id, status);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Update surgery consultation notes",
            description = "Update doctor notes for a surgery consultation. Only ADMIN and DOCTOR can update."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Notes updated successfully",
                    content = @Content(schema = @Schema(implementation = SurgeryConsultationResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Only ADMIN and DOCTOR can update"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Surgery consultation not found")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @PutMapping("/{id}/notes")
    public ResponseEntity<SurgeryConsultationResponse> updateSurgeryConsultationNotes(
            @PathVariable Long id,
            @RequestParam String notes) {
        SurgeryConsultationResponse response = surgeryService.updateSurgeryConsultationNotes(id, notes);
        return ResponseEntity.ok(response);
    }
}






