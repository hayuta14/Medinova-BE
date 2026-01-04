package com.project.medinova.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "emergencies")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Emergency {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private User patient; // Có thể null nếu chưa có thông tin bệnh nhân

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clinic_id", nullable = false)
    private Clinic clinic;

    @Column(name = "patient_lat", nullable = false)
    private Double patientLat;

    @Column(name = "patient_lng", nullable = false)
    private Double patientLng;

    @Column(name = "patient_address", columnDefinition = "TEXT")
    private String patientAddress;

    @Column(name = "patient_name")
    private String patientName;

    @Column(name = "patient_phone")
    private String patientPhone;

    @Column(columnDefinition = "TEXT")
    private String description; // Mô tả tình trạng khẩn cấp

    @Column(nullable = false)
    private String status; // PENDING | DISPATCHED | IN_TRANSIT | ARRIVED | COMPLETED | CANCELLED

    @Column(name = "priority")
    private String priority; // LOW | MEDIUM | HIGH | CRITICAL

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "dispatched_at")
    private LocalDateTime dispatchedAt;

    @Column(name = "arrived_at")
    private LocalDateTime arrivedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = "PENDING";
        }
    }
}


