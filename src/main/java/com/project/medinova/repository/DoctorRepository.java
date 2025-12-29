package com.project.medinova.repository;

import com.project.medinova.entity.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Optional<Doctor> findByUserId(Long userId);
    List<Doctor> findByClinicId(Long clinicId);
    List<Doctor> findBySpecialization(String specialization);

    @Query(value = """
        SELECT d.*
        FROM doctors d
        INNER JOIN users u ON d.user_id = u.id
        WHERE LOWER(COALESCE(u.full_name, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
          AND (:clinicId IS NULL OR d.clinic_id = :clinicId)
        ORDER BY 
            CASE 
                WHEN LOWER(u.full_name) = LOWER(:searchTerm) THEN 1
                WHEN LOWER(u.full_name) LIKE LOWER(CONCAT(:searchTerm, '%')) THEN 2
                WHEN LOWER(u.full_name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) THEN 3
                ELSE 4
            END,
            LENGTH(u.full_name),
            u.full_name
        """, 
        countQuery = """
        SELECT COUNT(*)
        FROM doctors d
        INNER JOIN users u ON d.user_id = u.id
        WHERE LOWER(COALESCE(u.full_name, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
          AND (:clinicId IS NULL OR d.clinic_id = :clinicId)
        """,
        nativeQuery = true)
    Page<Doctor> searchDoctors(@Param("searchTerm") String searchTerm, @Param("clinicId") Long clinicId, Pageable pageable);
}

