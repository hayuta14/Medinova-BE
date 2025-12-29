package com.project.medinova.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.medinova.dto.UpdateUserRoleRequest;
import com.project.medinova.entity.User;
import com.project.medinova.exception.NotFoundException;
import com.project.medinova.service.UserService;
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
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private ObjectMapper objectMapper;

    private User user;
    private UpdateUserRoleRequest updateRoleRequest;

    @BeforeEach
    void setUp() {
        com.project.medinova.exception.GlobalExceptionHandler exceptionHandler = 
            new com.project.medinova.exception.GlobalExceptionHandler();
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(exceptionHandler)
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setFullName("Test User");
        user.setPhone("0123456789");
        user.setRole("PATIENT");
        user.setStatus("ACTIVE");
        user.setCreatedAt(LocalDateTime.now());

        updateRoleRequest = new UpdateUserRoleRequest();
        updateRoleRequest.setRole("DOCTOR");
        updateRoleRequest.setClinicId(1L); // Required when role is DOCTOR
    }

    @Test
    void testGetAllUsers_Success() throws Exception {
        List<User> users = Arrays.asList(user);
        Page<User> userPage = new PageImpl<>(users, PageRequest.of(0, 10), 1);

        when(userService.getAllUsers(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(userPage);

        mockMvc.perform(get("/api/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].email").value("test@example.com"));
    }

    @Test
    void testGetUserById_Success() throws Exception {
        when(userService.getUserById(1L)).thenReturn(user);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void testGetUserById_NotFound() throws Exception {
        when(userService.getUserById(999L))
                .thenThrow(new NotFoundException("User not found with id: 999"));

        mockMvc.perform(get("/api/users/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateUserRole_Success() throws Exception {
        user.setRole("DOCTOR");
        when(userService.updateUserRole(eq(1L), any(UpdateUserRoleRequest.class))).thenReturn(user);

        mockMvc.perform(put("/api/users/1/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRoleRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("DOCTOR"));
    }

    @Test
    void testUpdateUserRole_NotFound() throws Exception {
        when(userService.updateUserRole(eq(999L), any(UpdateUserRoleRequest.class)))
                .thenThrow(new NotFoundException("User not found with id: 999"));

        mockMvc.perform(put("/api/users/999/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRoleRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateUserRole_InvalidRole() throws Exception {
        UpdateUserRoleRequest invalidRequest = new UpdateUserRoleRequest();
        invalidRequest.setRole("INVALID_ROLE");

        mockMvc.perform(put("/api/users/1/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateUserRole_ValidationError() throws Exception {
        UpdateUserRoleRequest invalidRequest = new UpdateUserRoleRequest();
        invalidRequest.setRole(""); // Empty role

        mockMvc.perform(put("/api/users/1/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
