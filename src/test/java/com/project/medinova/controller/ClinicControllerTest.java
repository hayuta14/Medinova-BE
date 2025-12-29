package com.project.medinova.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.medinova.dto.CreateClinicRequest;
import com.project.medinova.dto.UpdateClinicRequest;
import com.project.medinova.entity.Clinic;
import com.project.medinova.exception.BadRequestException;
import com.project.medinova.exception.NotFoundException;
import com.project.medinova.service.ClinicService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ClinicControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ClinicService clinicService;

    @InjectMocks
    private ClinicController clinicController;

    private ObjectMapper objectMapper;

    private Clinic clinic;
    private CreateClinicRequest createRequest;
    private UpdateClinicRequest updateRequest;

    @BeforeEach
    void setUp() {
        com.project.medinova.exception.GlobalExceptionHandler exceptionHandler = 
            new com.project.medinova.exception.GlobalExceptionHandler();
        mockMvc = MockMvcBuilders.standaloneSetup(clinicController)
                .setControllerAdvice(exceptionHandler)
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        clinic = new Clinic();
        clinic.setId(1L);
        clinic.setName("Test Clinic");
        clinic.setAddress("123 Test Street");
        clinic.setPhone("0123456789");
        clinic.setDescription("Test Description");
        clinic.setCreatedAt(LocalDateTime.now());

        createRequest = new CreateClinicRequest();
        createRequest.setName("New Clinic");
        createRequest.setAddress("456 New Street");
        createRequest.setPhone("0987654321");
        createRequest.setDescription("New Description");

        updateRequest = new UpdateClinicRequest();
        updateRequest.setName("Updated Clinic");
        updateRequest.setAddress("789 Updated Street");
    }

    @Test
    void testCreateClinic_Success() throws Exception {
        when(clinicService.createClinic(any(CreateClinicRequest.class))).thenReturn(clinic);

        mockMvc.perform(post("/api/clinics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Clinic"));
    }

    @Test
    void testCreateClinic_ValidationError() throws Exception {
        CreateClinicRequest invalidRequest = new CreateClinicRequest();
        invalidRequest.setName(""); // Empty name

        mockMvc.perform(post("/api/clinics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetClinicById_Success() throws Exception {
        when(clinicService.getClinicById(1L)).thenReturn(clinic);

        mockMvc.perform(get("/api/clinics/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Clinic"));
    }

    @Test
    void testGetClinicById_NotFound() throws Exception {
        when(clinicService.getClinicById(999L))
                .thenThrow(new NotFoundException("Clinic not found with id: 999"));

        mockMvc.perform(get("/api/clinics/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllClinics_Success() throws Exception {
        List<Clinic> clinics = Arrays.asList(clinic);
        Page<Clinic> clinicPage = new PageImpl<>(clinics, PageRequest.of(0, 10), 1);

        when(clinicService.getAllClinics(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(clinicPage);

        mockMvc.perform(get("/api/clinics")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void testUpdateClinic_Success() throws Exception {
        clinic.setName("Updated Clinic");
        when(clinicService.updateClinic(eq(1L), any(UpdateClinicRequest.class))).thenReturn(clinic);

        mockMvc.perform(put("/api/clinics/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Clinic"));
    }

    @Test
    void testUpdateClinic_NotFound() throws Exception {
        when(clinicService.updateClinic(eq(999L), any(UpdateClinicRequest.class)))
                .thenThrow(new NotFoundException("Clinic not found with id: 999"));

        mockMvc.perform(put("/api/clinics/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteClinic_Success() throws Exception {
        doNothing().when(clinicService).deleteClinic(1L);

        mockMvc.perform(delete("/api/clinics/1"))
                .andExpect(status().isNoContent());

        verify(clinicService, times(1)).deleteClinic(1L);
    }

    @Test
    void testDeleteClinic_NotFound() throws Exception {
        doThrow(new NotFoundException("Clinic not found with id: 999"))
                .when(clinicService).deleteClinic(999L);

        mockMvc.perform(delete("/api/clinics/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteClinic_HasDoctors() throws Exception {
        doThrow(new BadRequestException("Cannot delete clinic. There are 2 doctor(s) associated with this clinic. Please reassign or remove doctors first."))
                .when(clinicService).deleteClinic(1L);

        mockMvc.perform(delete("/api/clinics/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cannot delete clinic. There are 2 doctor(s) associated with this clinic. Please reassign or remove doctors first."));
    }

    @Test
    void testDeleteClinic_HasAppointments() throws Exception {
        doThrow(new BadRequestException("Cannot delete clinic. There are 5 appointment(s) associated with this clinic. Please handle appointments first."))
                .when(clinicService).deleteClinic(1L);

        mockMvc.perform(delete("/api/clinics/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cannot delete clinic. There are 5 appointment(s) associated with this clinic. Please handle appointments first."));
    }

    @Test
    void testDeleteClinic_HasSchedules() throws Exception {
        doThrow(new BadRequestException("Cannot delete clinic. There are 10 doctor schedule(s) associated with this clinic. Please handle schedules first."))
                .when(clinicService).deleteClinic(1L);

        mockMvc.perform(delete("/api/clinics/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cannot delete clinic. There are 10 doctor schedule(s) associated with this clinic. Please handle schedules first."));
    }
}
