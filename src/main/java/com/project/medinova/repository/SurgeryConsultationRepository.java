package com.project.medinova.repository;

import com.project.medinova.entity.SurgeryConsultation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SurgeryConsultationRepository extends JpaRepository<SurgeryConsultation, Long> {
    List<SurgeryConsultation> findByPatientId(Long patientId);
    List<SurgeryConsultation> findByDoctorId(Long doctorId);
    List<SurgeryConsultation> findByClinicId(Long clinicId);
    List<SurgeryConsultation> findByStatus(String status);
    List<SurgeryConsultation> findByPatientIdAndStatus(Long patientId, String status);
    List<SurgeryConsultation> findByDoctorIdAndStatus(Long doctorId, String status);
}






