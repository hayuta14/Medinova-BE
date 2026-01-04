package com.project.medinova.repository;

import com.project.medinova.entity.DoctorLeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DoctorLeaveRequestRepository extends JpaRepository<DoctorLeaveRequest, Long> {
    List<DoctorLeaveRequest> findByDoctorId(Long doctorId);
    List<DoctorLeaveRequest> findByStatus(String status);
    List<DoctorLeaveRequest> findByDoctorIdAndStatus(Long doctorId, String status);
    List<DoctorLeaveRequest> findByStartDateLessThanEqualAndEndDateGreaterThanEqualAndStatus(
            LocalDate endDate, LocalDate startDate, String status);
    long countByStatus(String status);
}

