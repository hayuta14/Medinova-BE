package com.project.medinova.repository;

import com.project.medinova.entity.Clinic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClinicRepository extends JpaRepository<Clinic, Long> {
    // Tìm clinics có vị trí (lat/lng) và có thể xử lý emergency
    @Query("SELECT c FROM Clinic c WHERE c.latitude IS NOT NULL AND c.longitude IS NOT NULL AND (c.emergencyEnabled IS NULL OR c.emergencyEnabled = true) AND (c.isActive IS NULL OR c.isActive = true)")
    List<Clinic> findAllWithLocationAndEmergencyEnabled();
    
    // Tìm clinics có thể xử lý emergency (có hoặc không có location)
    @Query("SELECT c FROM Clinic c WHERE (c.emergencyEnabled IS NULL OR c.emergencyEnabled = true) AND (c.isActive IS NULL OR c.isActive = true)")
    List<Clinic> findAllEmergencyEnabled();
}

