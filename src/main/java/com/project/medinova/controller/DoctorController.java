package com.project.medinova.controller;

import com.project.medinova.dto.CreateDoctorRequest;
import com.project.medinova.dto.UpdateDoctorRequest;
import com.project.medinova.dto.UpdateDoctorStatusRequest;
import com.project.medinova.entity.Department;
import com.project.medinova.entity.Doctor;
import com.project.medinova.entity.DoctorUpdateRequest;
import com.project.medinova.service.DoctorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/doctors")
@CrossOrigin(origins = "*")
@Tag(name = "Doctor Management", description = "Doctor CRUD operations APIs")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    @Operation(
            summary = "Create doctor",
            description = "Create a new doctor profile. User must have DOCTOR role and not be a doctor already. (ADMIN only)",
            tags = {"Doctor Management"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Doctor created successfully",
                    content = @Content(schema = @Schema(implementation = Doctor.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Only ADMIN can create doctor"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request - Validation error, user is already a doctor, or user doesn't have DOCTOR role"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not Found - User or Clinic not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Doctor> createDoctor(@Valid @RequestBody CreateDoctorRequest request) {
        Doctor doctor = doctorService.createDoctor(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(doctor);
    }

    @Operation(
            summary = "Get doctor by ID",
            description = "Retrieve doctor information by doctor ID. Returns doctor details including user info, clinic, specialization, and schedule.",
            tags = {"Doctor Management"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Doctor retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Doctor.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not Found - Doctor with the given ID does not exist")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Doctor> getDoctorById(@PathVariable Long id) {
        Doctor doctor = doctorService.getDoctorById(id);
        return ResponseEntity.ok(doctor);
    }

    @Operation(
            summary = "Get all doctors",
            description = "Retrieve a paginated list of all doctors. Supports pagination with page and size parameters.",
            tags = {"Doctor Management"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Doctors retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Doctor.class))
            )
    })
    @GetMapping
    public ResponseEntity<List<Doctor>> getAllDoctors(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {
        
        if (page < 0) page = 0;
        if (size < 1) size = 10;
        if (size > 100) size = 100; // Limit max page size
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Doctor> doctorPage = doctorService.getAllDoctors(pageable);
        List<Doctor> doctors = doctorPage.getContent();
        
        return ResponseEntity.ok(doctors);
    }

    @Operation(
            summary = "Get doctors by clinic",
            description = "Retrieve all doctors working at a specific clinic by clinic ID.",
            tags = {"Doctor Management"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Doctors retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Doctor.class))
            )
    })
    @GetMapping("/clinic/{clinicId}")
    public ResponseEntity<List<Doctor>> getDoctorsByClinic(@PathVariable Long clinicId) {
        List<Doctor> doctors = doctorService.getDoctorsByClinic(clinicId);
        return ResponseEntity.ok(doctors);
    }

    @Operation(
            summary = "Get doctors by department",
            description = "Retrieve all doctors in a specific department (e.g., CARDIOLOGY, NEUROLOGY, PEDIATRICS).",
            tags = {"Doctor Management"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Doctors retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Doctor.class))
            )
    })
    @GetMapping("/department/{department}")
    public ResponseEntity<List<Doctor>> getDoctorsByDepartment(@PathVariable Department department) {
        List<Doctor> doctors = doctorService.getDoctorsByDepartment(department);
        return ResponseEntity.ok(doctors);
    }

    @Operation(
            summary = "Get doctors by clinic and department",
            description = "Retrieve all doctors in a specific clinic and department.",
            tags = {"Doctor Management"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Doctors retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Doctor.class))
            )
    })
    @GetMapping("/clinic/{clinicId}/department/{department}")
    public ResponseEntity<List<Doctor>> getDoctorsByClinicAndDepartment(
            @PathVariable Long clinicId,
            @PathVariable Department department) {
        List<Doctor> doctors = doctorService.getDoctorsByClinicAndDepartment(clinicId, department);
        return ResponseEntity.ok(doctors);
    }

    @Operation(
            summary = "Search doctors",
            description = "Search doctors by full name with autocomplete/suggestion support. Case-insensitive search that matches partial names. Results are ranked by relevance (exact match > starts with > contains). Optional filter by clinicId to search doctors in a specific clinic. Supports pagination. Returns paginated response with content, page info, and total elements.",
            tags = {"Doctor Management"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Doctors retrieved successfully with pagination metadata",
                    content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Clinic not found (when clinicId is provided)")
    })
    @GetMapping("/search")
    public ResponseEntity<Page<Doctor>> searchDoctors(
            @RequestParam(required = false) String q,
            @RequestParam(required = false, defaultValue = "1") Long clinicId,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {
        
        if (page < 0) page = 0;
        if (size < 1) size = 10;
        if (size > 100) size = 100; // Limit max page size
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Doctor> doctorPage = doctorService.searchDoctors(q, clinicId, pageable);
        return ResponseEntity.ok(doctorPage);
    }

    @Operation(
            summary = "Update doctor",
            description = "Update doctor information. ADMIN can update any doctor. DOCTOR can only update their own information.",
            tags = {"Doctor Management"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Doctor updated successfully",
                    content = @Content(schema = @Schema(implementation = Doctor.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - DOCTOR trying to update another doctor's information"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not Found - Doctor not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request - Validation error")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @PutMapping("/{id}")
    public ResponseEntity<Doctor> updateDoctor(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDoctorRequest request) {
        Doctor doctor = doctorService.updateDoctor(id, request);
        return ResponseEntity.ok(doctor);
    }

    @Operation(
            summary = "Delete doctor",
            description = "Delete a doctor profile. This will remove the doctor from the system. (ADMIN only)",
            tags = {"Doctor Management"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Doctor deleted successfully - No content returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Only ADMIN can delete doctor"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not Found - Doctor not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDoctor(@PathVariable Long id) {
        doctorService.deleteDoctor(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Get pending doctors",
            description = "Get all doctors with PENDING status. Includes total count of pending doctors. (ADMIN only)",
            tags = {"Doctor Management"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Pending doctors retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Map.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Only ADMIN can access")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/pending")
    public ResponseEntity<Map<String, Object>> getPendingDoctors() {
        // Lấy các doctors có pending update requests
        List<DoctorUpdateRequest> pendingRequests = doctorService.getPendingUpdateRequests();
        long totalPendingCount = doctorService.getPendingUpdateRequestsCount();
        
        // Convert to list of doctors with pending requests
        List<Doctor> pendingDoctors = pendingRequests.stream()
                .map(DoctorUpdateRequest::getDoctor)
                .distinct()
                .toList();
        
        Map<String, Object> response = new HashMap<>();
        response.put("doctors", pendingDoctors);
        response.put("updateRequests", pendingRequests);
        response.put("totalPendingCount", totalPendingCount);
        
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Update doctor status",
            description = "Update doctor status (APPROVED or REJECTED). (ADMIN only)",
            tags = {"Doctor Management"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Doctor status updated successfully",
                    content = @Content(schema = @Schema(implementation = Doctor.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Only ADMIN can update doctor status"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not Found - Doctor not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request - Validation error or invalid status")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/status")
    public ResponseEntity<Doctor> updateDoctorStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDoctorStatusRequest request) {
        Doctor doctor = doctorService.updateDoctorStatus(id, request.getStatus());
        return ResponseEntity.ok(doctor);
    }
}

