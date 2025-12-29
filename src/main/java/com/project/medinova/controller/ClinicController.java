package com.project.medinova.controller;

import com.project.medinova.dto.CreateClinicRequest;
import com.project.medinova.dto.UpdateClinicRequest;
import com.project.medinova.entity.Clinic;
import com.project.medinova.service.ClinicService;
import io.swagger.v3.oas.annotations.Operation;
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

import java.util.List;

@RestController
@RequestMapping("/api/clinics")
@CrossOrigin(origins = "*")
@Tag(name = "Clinic Management", description = "Clinic CRUD operations APIs")
public class ClinicController {

    @Autowired
    private ClinicService clinicService;

    @Operation(summary = "Create clinic", description = "Create a new clinic (ADMIN only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Clinic created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Only ADMIN can create clinic"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Clinic> createClinic(@Valid @RequestBody CreateClinicRequest request) {
        Clinic clinic = clinicService.createClinic(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(clinic);
    }

    @Operation(summary = "Get clinic by ID", description = "Get clinic information by ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Clinic retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Clinic not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Clinic> getClinicById(@PathVariable Long id) {
        Clinic clinic = clinicService.getClinicById(id);
        return ResponseEntity.ok(clinic);
    }

    @Operation(summary = "Get all clinics", description = "Get list of all clinics with optional pagination")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Clinics retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<List<Clinic>> getAllClinics(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {
        
        if (page < 0) page = 0;
        if (size < 1) size = 10;
        if (size > 100) size = 100; // Limit max page size
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Clinic> clinicPage = clinicService.getAllClinics(pageable);
        List<Clinic> clinics = clinicPage.getContent();
        
        return ResponseEntity.ok(clinics);
    }

    @Operation(summary = "Update clinic", description = "Update clinic information (ADMIN only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Clinic updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Only ADMIN can update clinic"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Clinic not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Clinic> updateClinic(
            @PathVariable Long id,
            @Valid @RequestBody UpdateClinicRequest request) {
        Clinic clinic = clinicService.updateClinic(id, request);
        return ResponseEntity.ok(clinic);
    }

    @Operation(
            summary = "Delete clinic",
            description = "Delete a clinic by ID. Cannot delete if clinic has associated doctors, appointments, or schedules. (ADMIN only)",
            tags = {"Clinic Management"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Clinic deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request - Clinic has associated doctors, appointments, or schedules"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Only ADMIN can delete clinic"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Clinic not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClinic(@PathVariable Long id) {
        clinicService.deleteClinic(id);
        return ResponseEntity.noContent().build();
    }
}

