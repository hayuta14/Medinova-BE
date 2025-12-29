package com.project.medinova.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.medinova.dto.AuthRequest;
import com.project.medinova.dto.AuthResponse;
import com.project.medinova.dto.RegisterRequest;
import com.project.medinova.dto.TokenValidationResponse;
import com.project.medinova.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private ObjectMapper objectMapper;

    private AuthRequest authRequest;
    private AuthResponse authResponse;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        com.project.medinova.exception.GlobalExceptionHandler exceptionHandler = 
            new com.project.medinova.exception.GlobalExceptionHandler();
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(exceptionHandler)
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        authRequest = new AuthRequest();
        authRequest.setEmail("test@example.com");
        authRequest.setPassword("password123");

        authResponse = new AuthResponse();
        authResponse.setToken("test-token");
        authResponse.setTokenType("Bearer");
        authResponse.setUserId(1L);
        authResponse.setEmail("test@example.com");
        authResponse.setFullName("Test User");
        authResponse.setRole("PATIENT");

        registerRequest = new RegisterRequest();
        registerRequest.setEmail("newuser@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFullName("New User");
        registerRequest.setPhone("0123456789");
    }

    @Test
    void testLogin_Success() throws Exception {
        when(authService.login(any(AuthRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test-token"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void testLogin_InvalidCredentials() throws Exception {
        when(authService.login(any(AuthRequest.class)))
                .thenThrow(new com.project.medinova.exception.UnauthorizedException("Invalid email or password"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testLogin_ValidationError() throws Exception {
        AuthRequest invalidRequest = new AuthRequest();
        invalidRequest.setEmail(""); // Empty email

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRegister_Success() throws Exception {
        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("test-token"));
    }

    @Test
    void testRegister_EmailExists() throws Exception {
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new com.project.medinova.exception.BadRequestException("Email already exists"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLogout_Success() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testValidateToken_Success() throws Exception {
        TokenValidationResponse validationResponse = new TokenValidationResponse();
        validationResponse.setValid(true);
        validationResponse.setExpired(false);
        validationResponse.setMessage("Token is valid");
        validationResponse.setUserId(1L);
        validationResponse.setEmail("test@example.com");
        validationResponse.setRole("PATIENT");

        when(authService.validateToken(any(String.class))).thenReturn(validationResponse);

        mockMvc.perform(post("/api/auth/validate-token")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.expired").value(false));
    }

    @Test
    void testValidateToken_Expired() throws Exception {
        TokenValidationResponse validationResponse = new TokenValidationResponse();
        validationResponse.setValid(false);
        validationResponse.setExpired(true);
        validationResponse.setMessage("Token is expired");

        when(authService.validateToken(any(String.class))).thenReturn(validationResponse);

        mockMvc.perform(post("/api/auth/validate-token")
                        .header("Authorization", "Bearer expired-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.expired").value(true));
    }
}
