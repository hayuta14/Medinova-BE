package com.project.medinova.repository;

import com.project.medinova.entity.PatientMedicalHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientMedicalHistoryRepository extends JpaRepository<PatientMedicalHistory, Long> {
    List<PatientMedicalHistory> findByPatientId(Long patientId);
    Optional<PatientMedicalHistory> findFirstByPatientIdOrderByCreatedAtDesc(Long patientId);
}

