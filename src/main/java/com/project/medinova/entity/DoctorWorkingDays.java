package com.project.medinova.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "doctor_working_days")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorWorkingDays {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(name = "day_of_week", nullable = false)
    private Integer dayOfWeek; // 1=Mon ... 7=Sun

    @Column(name = "is_working", nullable = false)
    private Boolean isWorking = true; // true by default
}

