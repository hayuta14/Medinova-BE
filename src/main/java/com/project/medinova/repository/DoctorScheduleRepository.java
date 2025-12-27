package com.project.medinova.repository;

import com.project.medinova.entity.DoctorSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DoctorScheduleRepository extends JpaRepository<DoctorSchedule, Long> {
    List<DoctorSchedule> findByDoctorId(Long doctorId);
    List<DoctorSchedule> findByClinicId(Long clinicId);
    List<DoctorSchedule> findByDoctorIdAndWorkDate(Long doctorId, LocalDate workDate);
    List<DoctorSchedule> findByDoctorIdAndWorkDateBetween(Long doctorId, LocalDate startDate, LocalDate endDate);
    List<DoctorSchedule> findByStatus(String status);
    List<DoctorSchedule> findByDoctorIdAndStatus(Long doctorId, String status);
}

