package com.project.medinova.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "blood_tests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BloodTest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clinic_id", nullable = false)
    private Clinic clinic;

    @Column(nullable = false)
    private String testType; // CBC, Blood Glucose, Lipid Panel, etc.

    @Column(name = "test_date", nullable = false)
    private LocalDateTime testDate;

    @Column(name = "test_time", nullable = false)
    private String testTime; // HH:mm format

    @Column(nullable = false)
    private String status; // PENDING | SCHEDULED | COMPLETED | CANCELLED

    @Column(name = "result_file_url")
    private String resultFileUrl; // URL to result file (PDF/image)

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "price")
    private Double price;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

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



