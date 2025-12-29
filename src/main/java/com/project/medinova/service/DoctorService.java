package com.project.medinova.service;

import com.project.medinova.dto.CreateDoctorRequest;
import com.project.medinova.dto.UpdateDoctorRequest;
import com.project.medinova.entity.Clinic;
import com.project.medinova.entity.Doctor;
import com.project.medinova.entity.User;
import com.project.medinova.exception.BadRequestException;
import com.project.medinova.exception.NotFoundException;
import com.project.medinova.exception.ForbiddenException;
import com.project.medinova.repository.ClinicRepository;
import com.project.medinova.repository.DoctorRepository;
import com.project.medinova.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DoctorService {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClinicRepository clinicRepository;

    @Autowired
    private AuthService authService;

    public Doctor createDoctor(CreateDoctorRequest request) {
        // Kiểm tra user tồn tại và có role DOCTOR
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new NotFoundException("User not found with id: " + request.getUserId()));

        if (!"DOCTOR".equals(user.getRole())) {
            throw new BadRequestException("User must have DOCTOR role");
        }

        // Kiểm tra user đã là doctor chưa
        if (doctorRepository.findByUserId(request.getUserId()).isPresent()) {
            throw new BadRequestException("User is already a doctor");
        }

        // Kiểm tra clinic tồn tại
        Clinic clinic = clinicRepository.findById(request.getClinicId())
                .orElseThrow(() -> new NotFoundException("Clinic not found with id: " + request.getClinicId()));

        Doctor doctor = new Doctor();
        doctor.setUser(user);
        doctor.setClinic(clinic);
        doctor.setSpecialization(request.getSpecialization());
        doctor.setExperienceYears(request.getExperienceYears());
        doctor.setBio(request.getBio());
        doctor.setDefaultStartTime(request.getDefaultStartTime());
        doctor.setDefaultEndTime(request.getDefaultEndTime());

        return doctorRepository.save(doctor);
    }

    public Doctor getDoctorById(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Doctor not found with id: " + id));
    }

    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    public Page<Doctor> getAllDoctors(Pageable pageable) {
        return doctorRepository.findAll(pageable);
    }

    public List<Doctor> getDoctorsByClinic(Long clinicId) {
        return doctorRepository.findByClinicId(clinicId);
    }

    public List<Doctor> getDoctorsBySpecialization(String specialization) {
        return doctorRepository.findBySpecialization(specialization);
    }

    public Page<Doctor> searchDoctors(String searchTerm, Long clinicId, Pageable pageable) {
        // Nếu có clinicId, kiểm tra clinic tồn tại
        if (clinicId != null) {
            clinicRepository.findById(clinicId)
                    .orElseThrow(() -> new NotFoundException("Clinic not found with id: " + clinicId));
        }
        
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            // Nếu search term rỗng, trả về tất cả doctors (có thể filter theo clinicId)
            if (clinicId != null) {
                List<Doctor> doctors = doctorRepository.findByClinicId(clinicId);
                int start = (int) pageable.getOffset();
                int end = Math.min((start + pageable.getPageSize()), doctors.size());
                List<Doctor> pageContent = doctors.subList(start, end);
                return new PageImpl<>(pageContent, pageable, doctors.size());
            }
            return doctorRepository.findAll(pageable);
        }
        
        // Normalize search term - chỉ trim và normalize whitespace
        // Không cần sanitize vì đã dùng parameterized query với ILIKE
        String sanitizedTerm = searchTerm.trim().replaceAll("\\s+", " ").trim();
        
        if (sanitizedTerm.isEmpty()) {
            if (clinicId != null) {
                List<Doctor> doctors = doctorRepository.findByClinicId(clinicId);
                int start = (int) pageable.getOffset();
                int end = Math.min((start + pageable.getPageSize()), doctors.size());
                List<Doctor> pageContent = doctors.subList(start, end);
                return new PageImpl<>(pageContent, pageable, doctors.size());
            }
            return doctorRepository.findAll(pageable);
        }
        
        try {
            return doctorRepository.searchDoctors(sanitizedTerm, clinicId, pageable);
        } catch (Exception e) {
            // Nếu có lỗi, fallback về findAll hoặc findByClinicId
            if (clinicId != null) {
                List<Doctor> doctors = doctorRepository.findByClinicId(clinicId);
                int start = (int) pageable.getOffset();
                int end = Math.min((start + pageable.getPageSize()), doctors.size());
                List<Doctor> pageContent = doctors.subList(start, end);
                return new PageImpl<>(pageContent, pageable, doctors.size());
            }
            return doctorRepository.findAll(pageable);
        }
    }

    public Doctor updateDoctor(Long id, UpdateDoctorRequest request) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Doctor not found with id: " + id));

        // Kiểm tra quyền: DOCTOR chỉ có thể update chính mình, ADMIN có thể update bất kỳ
        var currentUser = authService.getCurrentUser();
        if (currentUser != null && "DOCTOR".equals(currentUser.getRole())) {
            if (!doctor.getUser().getId().equals(currentUser.getId())) {
                throw new ForbiddenException("Doctors can only update their own information");
            }
        }

        if (request.getClinicId() != null) {
            Clinic clinic = clinicRepository.findById(request.getClinicId())
                    .orElseThrow(() -> new NotFoundException("Clinic not found with id: " + request.getClinicId()));
            doctor.setClinic(clinic);
        }

        if (request.getSpecialization() != null) {
            doctor.setSpecialization(request.getSpecialization());
        }
        if (request.getExperienceYears() != null) {
            doctor.setExperienceYears(request.getExperienceYears());
        }
        if (request.getBio() != null) {
            doctor.setBio(request.getBio());
        }
        if (request.getDefaultStartTime() != null) {
            doctor.setDefaultStartTime(request.getDefaultStartTime());
        }
        if (request.getDefaultEndTime() != null) {
            doctor.setDefaultEndTime(request.getDefaultEndTime());
        }

        return doctorRepository.save(doctor);
    }

    public void deleteDoctor(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Doctor not found with id: " + id));

        doctorRepository.delete(doctor);
    }
}

