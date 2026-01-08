package com.project.medinova.repository;

import com.project.medinova.entity.Appointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
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
    
    // Query appointments by patient and appointment date
    @Query("SELECT a FROM Appointment a WHERE a.patient.id = :patientId AND DATE(a.appointmentTime) = :appointmentDate")
    List<Appointment> findByPatientIdAndAppointmentDate(@Param("patientId") Long patientId, @Param("appointmentDate") LocalDate appointmentDate);
    List<Appointment> findByStatusAndCreatedAtBefore(String status, LocalDateTime createdAt);
    
    // Query appointments by doctor and date range
    List<Appointment> findByDoctorIdAndAppointmentTimeBetween(Long doctorId, LocalDateTime startOfDay, LocalDateTime endOfDay);
    
    // Query appointments by doctor with paging and custom ordering
    // Orders: appointments >= now first (ASC), then appointments < now (DESC)
    @Query(value = """
        SELECT a.*
        FROM appointments a
        WHERE a.doctor_id = :doctorId
          AND (:status IS NULL OR a.status = :status)
        ORDER BY 
            CASE WHEN a.appointment_time >= :now THEN 0 ELSE 1 END,
            CASE WHEN a.appointment_time >= :now THEN a.appointment_time END ASC NULLS LAST,
            CASE WHEN a.appointment_time < :now THEN a.appointment_time END DESC NULLS LAST
        """,
        countQuery = """
        SELECT COUNT(*)
        FROM appointments a
        WHERE a.doctor_id = :doctorId
          AND (:status IS NULL OR a.status = :status)
        """,
        nativeQuery = true)
    Page<Appointment> findByDoctorIdWithOrdering(
        @Param("doctorId") Long doctorId,
        @Param("status") String status,
        @Param("now") LocalDateTime now,
        Pageable pageable);
}

