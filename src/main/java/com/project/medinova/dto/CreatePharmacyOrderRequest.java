package com.project.medinova.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreatePharmacyOrderRequest {
    @NotNull(message = "Clinic ID is required")
    private Long clinicId;

    private Long appointmentId; // Optional - if order is from appointment

    private String prescriptionFileUrl; // Optional - if prescription is uploaded

    @NotEmpty(message = "Order items are required")
    private List<PharmacyOrderItemRequest> items;

    @NotNull(message = "Delivery address is required")
    private String deliveryAddress;

    @NotNull(message = "Delivery phone is required")
    private String deliveryPhone;

    @NotNull(message = "Delivery name is required")
    private String deliveryName;

    private String paymentMethod; // CASH_ON_DELIVERY | CREDIT_CARD | BANK_TRANSFER

    private String notes;
}

