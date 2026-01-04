package com.project.medinova.controller;

import com.project.medinova.dto.BloodTestResponse;
import com.project.medinova.dto.CreateBloodTestRequest;
import com.project.medinova.service.BloodTestService;
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
@RequestMapping("/api/blood-tests")
@CrossOrigin(origins = "*")
@Tag(name = "Blood Test Management", description = "Blood test appointment and management APIs")
public class BloodTestController {

    @Autowired
    private BloodTestService bloodTestService;

    @Operation(
            summary = "Create blood test request",
            description = "Create a new blood test appointment request. Only patients can create requests."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Blood test request created successfully",
                    content = @Content(schema = @Schema(implementation = BloodTestResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad request - Validation error or invalid date/time"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Only patients can create requests"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Clinic not found")
    })
    @PreAuthorize("hasRole('PATIENT')")
    @PostMapping
    public ResponseEntity<BloodTestResponse> createBloodTest(@Valid @RequestBody CreateBloodTestRequest request) {
        BloodTestResponse response = bloodTestService.createBloodTest(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Get blood test by ID",
            description = "Get blood test information by ID. Patients can only view their own tests, ADMIN and DOCTOR can view all."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Blood test retrieved successfully",
                    content = @Content(schema = @Schema(implementation = BloodTestResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Cannot view other patients' tests"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Blood test not found")
    })
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<BloodTestResponse> getBloodTestById(@PathVariable Long id) {
        BloodTestResponse response = bloodTestService.getBloodTestById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get my blood tests",
            description = "Get all blood tests created by the current patient."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Blood tests retrieved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/my-tests")
    public ResponseEntity<List<BloodTestResponse>> getMyBloodTests() {
        List<BloodTestResponse> tests = bloodTestService.getMyBloodTests();
        return ResponseEntity.ok(tests);
    }

    @Operation(
            summary = "Get all blood tests",
            description = "Get all blood tests with optional status filter. Only ADMIN and DOCTOR can access."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Blood tests retrieved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Only ADMIN and DOCTOR can access")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @GetMapping
    public ResponseEntity<List<BloodTestResponse>> getAllBloodTests(
            @RequestParam(required = false) String status) {
        List<BloodTestResponse> tests = bloodTestService.getAllBloodTests(status);
        return ResponseEntity.ok(tests);
    }

    @Operation(
            summary = "Get blood tests by clinic",
            description = "Get all blood tests for a specific clinic with optional status filter. Only ADMIN and DOCTOR can access."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Blood tests retrieved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Only ADMIN and DOCTOR can access"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Clinic not found")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @GetMapping("/clinics/{clinicId}")
    public ResponseEntity<List<BloodTestResponse>> getBloodTestsByClinic(
            @PathVariable Long clinicId,
            @RequestParam(required = false) String status) {
        List<BloodTestResponse> tests = bloodTestService.getBloodTestsByClinic(clinicId, status);
        return ResponseEntity.ok(tests);
    }

    @Operation(
            summary = "Update blood test status",
            description = "Update blood test status (PENDING, SCHEDULED, COMPLETED, CANCELLED). Only ADMIN and DOCTOR can update."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Blood test status updated successfully",
                    content = @Content(schema = @Schema(implementation = BloodTestResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad request - Invalid status"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Only ADMIN and DOCTOR can update"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Blood test not found")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @PutMapping("/{id}/status")
    public ResponseEntity<BloodTestResponse> updateBloodTestStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        BloodTestResponse response = bloodTestService.updateBloodTestStatus(id, status);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Update blood test result",
            description = "Upload blood test result file URL. Only ADMIN and DOCTOR can update. Status will be automatically set to COMPLETED."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Blood test result updated successfully",
                    content = @Content(schema = @Schema(implementation = BloodTestResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Only ADMIN and DOCTOR can update"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Blood test not found")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @PutMapping("/{id}/result")
    public ResponseEntity<BloodTestResponse> updateBloodTestResult(
            @PathVariable Long id,
            @RequestParam String resultFileUrl) {
        BloodTestResponse response = bloodTestService.updateBloodTestResult(id, resultFileUrl);
        return ResponseEntity.ok(response);
    }
}

