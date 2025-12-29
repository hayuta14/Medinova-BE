package com.project.medinova.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.medinova.dto.PatientMedicalHistoryRequest;
import com.project.medinova.entity.PatientMedicalHistory;
import com.project.medinova.entity.UserProfile;
import com.project.medinova.service.UserProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserProfileControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserProfileService userProfileService;

    @InjectMocks
    private UserProfileController userProfileController;

    private ObjectMapper objectMapper;

    private UserProfile userProfile;
    private PatientMedicalHistory medicalHistory;
    private PatientMedicalHistoryRequest medicalHistoryRequest;

    @BeforeEach
    void setUp() {
        com.project.medinova.exception.GlobalExceptionHandler exceptionHandler = 
            new com.project.medinova.exception.GlobalExceptionHandler();
        mockMvc = MockMvcBuilders.standaloneSetup(userProfileController)
                .setControllerAdvice(exceptionHandler)
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        userProfile = new UserProfile();
        userProfile.setUserId(1L);

        medicalHistory = new PatientMedicalHistory();
        medicalHistory.setId(1L);
        medicalHistory.setMedicalCondition("Test Condition");
        medicalHistory.setDiagnosisDate(LocalDate.now());
        medicalHistory.setTreatmentDescription("Test Treatment");
        medicalHistory.setMedications("Test Medication");
        medicalHistory.setAllergies("Test Allergy");
        medicalHistory.setCreatedAt(LocalDateTime.now());

        medicalHistoryRequest = new PatientMedicalHistoryRequest();
        medicalHistoryRequest.setMedicalCondition("New Condition");
        medicalHistoryRequest.setDiagnosisDate(LocalDate.now());
        medicalHistoryRequest.setTreatmentDescription("New Treatment");
    }

    @Test
    void testGetUserProfile_Success() throws Exception {
        when(userProfileService.getUserProfile()).thenReturn(userProfile);

        mockMvc.perform(get("/api/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1L));
    }

    @Test
    void testUpdateMedicalHistory_Success() throws Exception {
        when(userProfileService.updateMedicalHistory(any(PatientMedicalHistoryRequest.class)))
                .thenReturn(medicalHistory);

        mockMvc.perform(put("/api/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(medicalHistoryRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.medicalCondition").value("Test Condition"));
    }

    @Test
    void testGetMedicalHistory_Success() throws Exception {
        when(userProfileService.getMedicalHistory()).thenReturn(medicalHistory);

        mockMvc.perform(get("/api/profile/medical-history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.medicalCondition").value("Test Condition"));
    }

    @Test
    void testGetMedicalHistory_NotFound() throws Exception {
        when(userProfileService.getMedicalHistory()).thenReturn(null);

        mockMvc.perform(get("/api/profile/medical-history"))
                .andExpect(status().isNoContent());
    }
}
