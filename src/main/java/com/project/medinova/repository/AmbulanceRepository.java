package com.project.medinova.repository;

import com.project.medinova.entity.Ambulance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AmbulanceRepository extends JpaRepository<Ambulance, Long> {
    List<Ambulance> findByClinicId(Long clinicId);
    List<Ambulance> findByStatus(String status);
    List<Ambulance> findByClinicIdAndStatus(Long clinicId, String status);
    List<Ambulance> findByClinicIdAndStatusAndCurrentLatIsNotNullAndCurrentLngIsNotNull(
            Long clinicId, String status);
}


