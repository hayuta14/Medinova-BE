package com.project.medinova.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "surgery_consultations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SurgeryConsultation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    private Doctor doctor; // Assigned doctor for consultation

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clinic_id", nullable = false)
    private Clinic clinic;

    @Column(nullable = false)
    private String surgeryType; // General Surgery, Cardiac Surgery, etc.

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(nullable = false)
    private String urgency; // ROUTINE | URGENT | EMERGENCY

    @Column(nullable = false)
    private String status; // PENDING | CONSULTATION_SCHEDULED | CONSULTATION_COMPLETED | SURGERY_SCHEDULED | SURGERY_COMPLETED | CANCELLED

    @Column(name = "consultation_appointment_id")
    private Long consultationAppointmentId; // Link to appointment for consultation

    @Column(name = "surgery_appointment_id")
    private Long surgeryAppointmentId; // Link to appointment for surgery

    @Column(columnDefinition = "TEXT")
    private String notes; // Doctor notes

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "consultation_date")
    private LocalDateTime consultationDate;

    @Column(name = "surgery_date")
    private LocalDateTime surgeryDate;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = "PENDING";
        }
    }
}

