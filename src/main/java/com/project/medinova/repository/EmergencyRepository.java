package com.project.medinova.repository;

import com.project.medinova.entity.Emergency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmergencyRepository extends JpaRepository<Emergency, Long> {
    List<Emergency> findByClinicId(Long clinicId);
    List<Emergency> findByStatus(String status);
    List<Emergency> findByClinicIdAndStatus(Long clinicId, String status);
}


