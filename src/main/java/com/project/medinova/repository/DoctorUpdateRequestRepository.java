package com.project.medinova.repository;

import com.project.medinova.entity.DoctorUpdateRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorUpdateRequestRepository extends JpaRepository<DoctorUpdateRequest, Long> {
    List<DoctorUpdateRequest> findByStatus(String status);
    
    List<DoctorUpdateRequest> findByDoctorId(Long doctorId);
    
    Optional<DoctorUpdateRequest> findByDoctorIdAndStatus(Long doctorId, String status);
    
    long countByStatus(String status);
}



