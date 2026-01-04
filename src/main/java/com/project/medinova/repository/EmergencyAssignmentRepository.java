package com.project.medinova.repository;

import com.project.medinova.entity.EmergencyAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmergencyAssignmentRepository extends JpaRepository<EmergencyAssignment, Long> {
    List<EmergencyAssignment> findByEmergencyId(Long emergencyId);
    List<EmergencyAssignment> findByAmbulanceId(Long ambulanceId);
    List<EmergencyAssignment> findByDoctorId(Long doctorId);
    Optional<EmergencyAssignment> findByEmergencyIdAndAmbulanceId(Long emergencyId, Long ambulanceId);
    
    // Đếm số emergency assignments của doctor trong ngày
    @Query("SELECT COUNT(ea) FROM EmergencyAssignment ea " +
           "WHERE ea.doctor.id = :doctorId " +
           "AND DATE(ea.assignedAt) = :date")
    Long countByDoctorIdAndDate(@Param("doctorId") Long doctorId, @Param("date") LocalDate date);
    
    // Tìm assignments của doctor đang active (emergency chưa completed/cancelled)
    @Query("SELECT ea FROM EmergencyAssignment ea " +
           "WHERE ea.doctor.id = :doctorId " +
           "AND ea.emergency.status IN ('PENDING', 'DISPATCHED', 'IN_TRANSIT')")
    List<EmergencyAssignment> findActiveAssignmentsByDoctorId(@Param("doctorId") Long doctorId);
}


