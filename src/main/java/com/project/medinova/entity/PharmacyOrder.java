package com.project.medinova.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "pharmacy_orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PharmacyOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
    private Appointment appointment; // Optional - if order is from appointment

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clinic_id", nullable = false)
    private Clinic clinic;

    @Column(name = "prescription_file_url")
    private String prescriptionFileUrl; // URL to uploaded prescription

    @Column(nullable = false)
    private String status; // PENDING | PROCESSING | READY | OUT_FOR_DELIVERY | DELIVERED | CANCELLED

    @Column(name = "delivery_address", columnDefinition = "TEXT")
    private String deliveryAddress;

    @Column(name = "delivery_phone")
    private String deliveryPhone;

    @Column(name = "delivery_name")
    private String deliveryName;

    @Column(name = "payment_method")
    private String paymentMethod; // CASH_ON_DELIVERY | CREDIT_CARD | BANK_TRANSFER

    @Column(name = "total_amount")
    private Double totalAmount;

    @Column(name = "delivery_fee")
    private Double deliveryFee;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = "PENDING";
        }
    }
}



