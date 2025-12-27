package com.project.medinova.controller;

import com.project.medinova.dto.ApiResponse;
import com.project.medinova.dto.PatientMedicalHistoryRequest;
import com.project.medinova.entity.PatientMedicalHistory;
import com.project.medinova.entity.UserProfile;
import com.project.medinova.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "*")
@Tag(name = "User Profile", description = "User profile and medical history management APIs")
public class UserProfileController {

    @Autowired
    private UserProfileService userProfileService;

    @Operation(summary = "Get user profile", description = "Get current user's profile information")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<UserProfile>> getUserProfile() {
        UserProfile profile = userProfileService.getUserProfile();
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @Operation(summary = "Update medical history", description = "Update medical history for current user based on JWT token")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Medical history updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PutMapping
    public ResponseEntity<ApiResponse<PatientMedicalHistory>> updateMedicalHistory(@Valid @RequestBody PatientMedicalHistoryRequest request) {
        PatientMedicalHistory medicalHistory = userProfileService.updateMedicalHistory(request);
        return ResponseEntity.ok(ApiResponse.success("Medical history updated successfully", medicalHistory));
    }

    @Operation(summary = "Get medical history", description = "Get current user's medical history")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Medical history retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Medical history not found")
    })
    @GetMapping("/medical-history")
    public ResponseEntity<ApiResponse<PatientMedicalHistory>> getMedicalHistory() {
        PatientMedicalHistory medicalHistory = userProfileService.getMedicalHistory();
        if (medicalHistory == null) {
            return ResponseEntity.ok(ApiResponse.success(null));
        }
        return ResponseEntity.ok(ApiResponse.success(medicalHistory));
    }
}

