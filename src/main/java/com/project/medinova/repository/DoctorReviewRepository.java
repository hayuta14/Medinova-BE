package com.project.medinova.repository;

import com.project.medinova.entity.DoctorReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoctorReviewRepository extends JpaRepository<DoctorReview, Long> {
    List<DoctorReview> findByDoctorId(Long doctorId);
    List<DoctorReview> findByPatientId(Long patientId);
    List<DoctorReview> findByDoctorIdAndPatientId(Long doctorId, Long patientId);
}

