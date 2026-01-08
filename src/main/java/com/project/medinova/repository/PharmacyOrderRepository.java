package com.project.medinova.repository;

import com.project.medinova.entity.PharmacyOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PharmacyOrderRepository extends JpaRepository<PharmacyOrder, Long> {
    List<PharmacyOrder> findByPatientId(Long patientId);
    List<PharmacyOrder> findByClinicId(Long clinicId);
    List<PharmacyOrder> findByStatus(String status);
    List<PharmacyOrder> findByPatientIdAndStatus(Long patientId, String status);
    List<PharmacyOrder> findByClinicIdAndStatus(Long clinicId, String status);
}






