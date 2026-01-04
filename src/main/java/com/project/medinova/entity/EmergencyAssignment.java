package com.project.medinova.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "emergency_assignments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emergency_id", nullable = false)
    private Emergency emergency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ambulance_id", nullable = true)
    private Ambulance ambulance; // Có thể null nếu không có xe (chỉ có bác sĩ)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    private Doctor doctor; // Có thể null nếu chỉ có paramedic

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "distance_km")
    private Double distanceKm; // Khoảng cách từ xe đến bệnh nhân (km)

    @PrePersist
    protected void onCreate() {
        if (assignedAt == null) {
            assignedAt = LocalDateTime.now();
        }
    }
}


