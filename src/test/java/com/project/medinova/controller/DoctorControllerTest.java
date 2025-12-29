package com.project.medinova.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.medinova.dto.CreateDoctorRequest;
import com.project.medinova.dto.UpdateDoctorRequest;
import com.project.medinova.entity.Clinic;
import com.project.medinova.entity.Doctor;
import com.project.medinova.entity.User;
import com.project.medinova.exception.BadRequestException;
import com.project.medinova.exception.NotFoundException;
import com.project.medinova.service.DoctorService;
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

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class DoctorControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DoctorService doctorService;

    @InjectMocks
    private DoctorController doctorController;

    private ObjectMapper objectMapper;

    private Doctor doctor;
    private User user;
    private Clinic clinic;
    private CreateDoctorRequest createRequest;
    private UpdateDoctorRequest updateRequest;

    @BeforeEach
    void setUp() {
        com.project.medinova.exception.GlobalExceptionHandler exceptionHandler = 
            new com.project.medinova.exception.GlobalExceptionHandler();
        mockMvc = MockMvcBuilders.standaloneSetup(doctorController)
                .setControllerAdvice(exceptionHandler)
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        user = new User();
        user.setId(2L);
        user.setEmail("doctor@example.com");
        user.setFullName("Dr. John Doe");
        user.setRole("DOCTOR");
        user.setStatus("ACTIVE");

        clinic = new Clinic();
        clinic.setId(1L);
        clinic.setName("Test Clinic");

        doctor = new Doctor();
        doctor.setId(1L);
        doctor.setUser(user);
        doctor.setClinic(clinic);
        doctor.setSpecialization("Cardiology");
        doctor.setExperienceYears(10);
        doctor.setBio("Experienced cardiologist");
        doctor.setDefaultStartTime(LocalTime.of(9, 0));
        doctor.setDefaultEndTime(LocalTime.of(17, 0));

        createRequest = new CreateDoctorRequest();
        createRequest.setUserId(2L);
        createRequest.setClinicId(1L);
        createRequest.setSpecialization("Cardiology");
        createRequest.setExperienceYears(10);
        createRequest.setBio("Experienced cardiologist");
        createRequest.setDefaultStartTime(LocalTime.of(9, 0));
        createRequest.setDefaultEndTime(LocalTime.of(17, 0));

        updateRequest = new UpdateDoctorRequest();
        updateRequest.setSpecialization("Neurology");
        updateRequest.setExperienceYears(15);
    }

    @Test
    void testCreateDoctor_Success() throws Exception {
        when(doctorService.createDoctor(any(CreateDoctorRequest.class))).thenReturn(doctor);

        mockMvc.perform(post("/api/doctors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.specialization").value("Cardiology"))
                .andExpect(jsonPath("$.experienceYears").value(10));
    }

    @Test
    void testCreateDoctor_ValidationError() throws Exception {
        CreateDoctorRequest invalidRequest = new CreateDoctorRequest();
        invalidRequest.setUserId(null); // Missing required field

        mockMvc.perform(post("/api/doctors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateDoctor_UserNotFound() throws Exception {
        when(doctorService.createDoctor(any(CreateDoctorRequest.class)))
                .thenThrow(new NotFoundException("User not found with id: 999"));

        mockMvc.perform(post("/api/doctors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateDoctor_UserNotDoctor() throws Exception {
        when(doctorService.createDoctor(any(CreateDoctorRequest.class)))
                .thenThrow(new BadRequestException("User must have DOCTOR role"));

        mockMvc.perform(post("/api/doctors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateDoctor_UserAlreadyDoctor() throws Exception {
        when(doctorService.createDoctor(any(CreateDoctorRequest.class)))
                .thenThrow(new BadRequestException("User is already a doctor"));

        mockMvc.perform(post("/api/doctors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetDoctorById_Success() throws Exception {
        when(doctorService.getDoctorById(1L)).thenReturn(doctor);

        mockMvc.perform(get("/api/doctors/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.specialization").value("Cardiology"));
    }

    @Test
    void testGetDoctorById_NotFound() throws Exception {
        when(doctorService.getDoctorById(999L))
                .thenThrow(new NotFoundException("Doctor not found with id: 999"));

        mockMvc.perform(get("/api/doctors/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllDoctors_Success() throws Exception {
        List<Doctor> doctors = Arrays.asList(doctor);
        Page<Doctor> doctorPage = new PageImpl<>(doctors, PageRequest.of(0, 10), 1);

        when(doctorService.getAllDoctors(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(doctorPage);

        mockMvc.perform(get("/api/doctors")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void testGetDoctorsByClinic_Success() throws Exception {
        List<Doctor> doctors = Arrays.asList(doctor);

        when(doctorService.getDoctorsByClinic(1L)).thenReturn(doctors);

        mockMvc.perform(get("/api/doctors/clinic/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].clinic.id").value(1L));
    }

    @Test
    void testGetDoctorsBySpecialization_Success() throws Exception {
        List<Doctor> doctors = Arrays.asList(doctor);

        when(doctorService.getDoctorsBySpecialization("Cardiology")).thenReturn(doctors);

        mockMvc.perform(get("/api/doctors/specialization/Cardiology"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].specialization").value("Cardiology"));
    }

    @Test
    void testUpdateDoctor_Success() throws Exception {
        doctor.setSpecialization("Neurology");
        when(doctorService.updateDoctor(eq(1L), any(UpdateDoctorRequest.class))).thenReturn(doctor);

        mockMvc.perform(put("/api/doctors/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.specialization").value("Neurology"));
    }

    @Test
    void testUpdateDoctor_NotFound() throws Exception {
        when(doctorService.updateDoctor(eq(999L), any(UpdateDoctorRequest.class)))
                .thenThrow(new NotFoundException("Doctor not found with id: 999"));

        mockMvc.perform(put("/api/doctors/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteDoctor_Success() throws Exception {
        doNothing().when(doctorService).deleteDoctor(1L);

        mockMvc.perform(delete("/api/doctors/1"))
                .andExpect(status().isNoContent());

        verify(doctorService, times(1)).deleteDoctor(1L);
    }

    @Test
    void testDeleteDoctor_NotFound() throws Exception {
        doThrow(new NotFoundException("Doctor not found with id: 999"))
                .when(doctorService).deleteDoctor(999L);

        mockMvc.perform(delete("/api/doctors/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testSearchDoctors_Success() throws Exception {
        List<Doctor> doctors = Arrays.asList(doctor);
        Page<Doctor> doctorPage = new PageImpl<>(doctors, PageRequest.of(0, 10), 1);

        when(doctorService.searchDoctors(eq("cardiology"), eq(null), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(doctorPage);

        mockMvc.perform(get("/api/doctors/search")
                        .param("q", "cardiology")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].specialization").value("Cardiology"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    void testSearchDoctors_EmptyQuery() throws Exception {
        List<Doctor> doctors = Arrays.asList(doctor);
        Page<Doctor> doctorPage = new PageImpl<>(doctors, PageRequest.of(0, 10), 1);

        when(doctorService.searchDoctors(eq(null), eq(null), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(doctorPage);

        mockMvc.perform(get("/api/doctors/search")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void testSearchDoctors_WithPagination() throws Exception {
        List<Doctor> doctors = Arrays.asList(doctor);
        Page<Doctor> doctorPage = new PageImpl<>(doctors, PageRequest.of(1, 5), 2);

        when(doctorService.searchDoctors(eq("doctor"), eq(null), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(doctorPage);

        mockMvc.perform(get("/api/doctors/search")
                        .param("q", "doctor")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.first").value(false))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    void testSearchDoctors_WithClinicId() throws Exception {
        List<Doctor> doctors = Arrays.asList(doctor);
        Page<Doctor> doctorPage = new PageImpl<>(doctors, PageRequest.of(0, 10), 1);

        when(doctorService.searchDoctors(eq("Nguyen"), eq(1L), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(doctorPage);

        mockMvc.perform(get("/api/doctors/search")
                        .param("q", "Nguyen")
                        .param("clinicId", "1")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].clinic.id").value(1L));
    }
}

