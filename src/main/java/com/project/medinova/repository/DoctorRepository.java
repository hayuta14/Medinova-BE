package com.project.medinova.repository;

import com.project.medinova.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Optional<Doctor> findByUserId(Long userId);
    List<Doctor> findByClinicId(Long clinicId);
    List<Doctor> findBySpecialization(String specialization);
}

