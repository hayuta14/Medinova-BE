package com.project.medinova.repository;

import com.project.medinova.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPatientId(Long patientId);
    List<Appointment> findByDoctorId(Long doctorId);
    List<Appointment> findByClinicId(Long clinicId);
    List<Appointment> findByStatus(String status);
    List<Appointment> findByPatientIdAndStatus(Long patientId, String status);
    List<Appointment> findByDoctorIdAndStatus(Long doctorId, String status);
    List<Appointment> findByAppointmentTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
}

