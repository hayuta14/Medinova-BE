package com.project.medinova.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.medinova.dto.BloodTestResponse;
import com.project.medinova.dto.CreateBloodTestRequest;
import com.project.medinova.exception.GlobalExceptionHandler;
import com.project.medinova.service.BloodTestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class BloodTestControllerTest {

    @Mock
    private BloodTestService bloodTestService;

    @InjectMocks
    private BloodTestController bloodTestController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(bloodTestController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    void testCreateBloodTest_Success() throws Exception {
        CreateBloodTestRequest request = new CreateBloodTestRequest();
        request.setClinicId(1L);
        request.setTestType("CBC");
        request.setTestDate(LocalDate.now().plusDays(1));
        request.setTestTime("08:00");

        BloodTestResponse response = new BloodTestResponse();
        response.setId(1L);
        response.setTestType("CBC");
        response.setStatus("PENDING");

        when(bloodTestService.createBloodTest(any(CreateBloodTestRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/blood-tests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.testType").value("CBC"));
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    void testCreateBloodTest_Forbidden() throws Exception {
        CreateBloodTestRequest request = new CreateBloodTestRequest();
        request.setClinicId(1L);
        request.setTestType("CBC");
        request.setTestDate(LocalDate.now().plusDays(1));
        request.setTestTime("08:00");

        mockMvc.perform(post("/api/blood-tests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testCreateBloodTest_Unauthorized() throws Exception {
        CreateBloodTestRequest request = new CreateBloodTestRequest();
        request.setClinicId(1L);
        request.setTestType("CBC");

        mockMvc.perform(post("/api/blood-tests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllBloodTests_Success() throws Exception {
        mockMvc.perform(get("/api/blood-tests")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    void testGetAllBloodTests_Forbidden() throws Exception {
        mockMvc.perform(get("/api/blood-tests")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}

