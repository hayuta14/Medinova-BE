package com.project.medinova.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "patient_medical_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientMedicalHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    @JsonIgnore
    private User patient;

    @Column(name = "medical_condition", columnDefinition = "TEXT")
    private String medicalCondition; // Tên bệnh/tình trạng sức khỏe

    @Column(name = "diagnosis_date")
    private LocalDate diagnosisDate; // Ngày chẩn đoán

    @Column(name = "treatment_description", columnDefinition = "TEXT")
    private String treatmentDescription; // Mô tả điều trị

    @Column(name = "medications", columnDefinition = "TEXT")
    private String medications; // Thuốc đang sử dụng

    @Column(name = "allergies", columnDefinition = "TEXT")
    private String allergies; // Dị ứng

    @Column(name = "chronic_diseases", columnDefinition = "TEXT")
    private String chronicDiseases; // Bệnh mãn tính

    @Column(name = "previous_surgeries", columnDefinition = "TEXT")
    private String previousSurgeries; // Phẫu thuật trước đó

    @Column(name = "family_history", columnDefinition = "TEXT")
    private String familyHistory; // Tiền sử gia đình

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes; // Ghi chú thêm

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

