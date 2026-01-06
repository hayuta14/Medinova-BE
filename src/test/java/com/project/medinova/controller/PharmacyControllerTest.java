package com.project.medinova.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.medinova.dto.CreatePharmacyOrderRequest;
import com.project.medinova.dto.PharmacyOrderItemRequest;
import com.project.medinova.dto.PharmacyOrderResponse;
import com.project.medinova.exception.GlobalExceptionHandler;
import com.project.medinova.service.PharmacyService;
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

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PharmacyControllerTest {

    @Mock
    private PharmacyService pharmacyService;

    @InjectMocks
    private PharmacyController pharmacyController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(pharmacyController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    void testCreatePharmacyOrder_Success() throws Exception {
        PharmacyOrderItemRequest item = new PharmacyOrderItemRequest();
        item.setMedicineName("Paracetamol");
        item.setQuantity(2);
        item.setPrice(10.0);

        CreatePharmacyOrderRequest request = new CreatePharmacyOrderRequest();
        request.setClinicId(1L);
        request.setItems(Arrays.asList(item));
        request.setDeliveryAddress("123 Main St");
        request.setDeliveryPhone("0123456789");
        request.setDeliveryName("John Doe");

        PharmacyOrderResponse response = new PharmacyOrderResponse();
        response.setId(1L);
        response.setStatus("PENDING");

        when(pharmacyService.createPharmacyOrder(any(CreatePharmacyOrderRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/pharmacy-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    void testCreatePharmacyOrder_Forbidden() throws Exception {
        CreatePharmacyOrderRequest request = new CreatePharmacyOrderRequest();
        request.setClinicId(1L);

        mockMvc.perform(post("/api/pharmacy-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllPharmacyOrders_Success() throws Exception {
        when(pharmacyService.getAllPharmacyOrders(null)).thenReturn(List.of());

        mockMvc.perform(get("/api/pharmacy-orders")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}




