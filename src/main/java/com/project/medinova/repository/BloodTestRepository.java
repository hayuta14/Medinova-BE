package com.project.medinova.repository;

import com.project.medinova.entity.BloodTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BloodTestRepository extends JpaRepository<BloodTest, Long> {
    List<BloodTest> findByPatientId(Long patientId);
    List<BloodTest> findByClinicId(Long clinicId);
    List<BloodTest> findByStatus(String status);
    List<BloodTest> findByPatientIdAndStatus(Long patientId, String status);
    List<BloodTest> findByClinicIdAndStatus(Long clinicId, String status);
    List<BloodTest> findByTestDateBetween(LocalDateTime start, LocalDateTime end);
}






