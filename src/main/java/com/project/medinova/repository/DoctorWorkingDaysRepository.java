package com.project.medinova.repository;

import com.project.medinova.entity.DoctorWorkingDays;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoctorWorkingDaysRepository extends JpaRepository<DoctorWorkingDays, Long> {
    List<DoctorWorkingDays> findByDoctorId(Long doctorId);
    DoctorWorkingDays findByDoctorIdAndDayOfWeek(Long doctorId, Integer dayOfWeek);
}

