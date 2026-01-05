package com.project.medinova.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PharmacyOrderResponse {
    private Long id;
    private Long patientId;
    private String patientName;
    private Long appointmentId;
    private Long clinicId;
    private String clinicName;
    private String prescriptionFileUrl;
    private String status;
    private String deliveryAddress;
    private String deliveryPhone;
    private String deliveryName;
    private String paymentMethod;
    private Double totalAmount;
    private Double deliveryFee;
    private String notes;
    private List<PharmacyOrderItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
    private LocalDateTime deliveredAt;
}



