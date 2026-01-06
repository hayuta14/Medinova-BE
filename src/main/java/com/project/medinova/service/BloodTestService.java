package com.project.medinova.service;

import com.project.medinova.dto.BloodTestResponse;
import com.project.medinova.dto.CreateBloodTestRequest;
import com.project.medinova.entity.BloodTest;
import com.project.medinova.entity.Clinic;
import com.project.medinova.entity.User;
import com.project.medinova.exception.BadRequestException;
import com.project.medinova.exception.ForbiddenException;
import com.project.medinova.exception.NotFoundException;
import com.project.medinova.repository.BloodTestRepository;
import com.project.medinova.repository.ClinicRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class BloodTestService {

    @Autowired
    private BloodTestRepository bloodTestRepository;

    @Autowired
    private ClinicRepository clinicRepository;

    @Autowired
    private AuthService authService;

    // Test type prices
    private static final double CBC_PRICE = 50.0;
    private static final double BLOOD_GLUCOSE_PRICE = 30.0;
    private static final double LIPID_PANEL_PRICE = 60.0;
    private static final double LIVER_FUNCTION_PRICE = 70.0;
    private static final double THYROID_FUNCTION_PRICE = 80.0;
    private static final double VITAMIN_D_PRICE = 90.0;

    public BloodTestResponse createBloodTest(CreateBloodTestRequest request) {
        User currentUser = authService.getCurrentUser();
        
        // Only PATIENT can create blood test requests
        if (!"PATIENT".equals(currentUser.getRole())) {
            throw new ForbiddenException("Only patients can create blood test requests");
        }

        Clinic clinic = clinicRepository.findById(request.getClinicId())
                .orElseThrow(() -> new NotFoundException("Clinic not found with id: " + request.getClinicId()));

        // Parse test date and time
        LocalDate testDate = request.getTestDate();
        String testTime = request.getTestTime();
        
        // Validate time format (HH:mm)
        try {
            LocalTime.parse(testTime, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception e) {
            throw new BadRequestException("Invalid time format. Use HH:mm format (e.g., 08:00)");
        }

        // Combine date and time
        LocalDateTime testDateTime = LocalDateTime.of(testDate, LocalTime.parse(testTime, DateTimeFormatter.ofPattern("HH:mm")));
        
        // Check if test date is in the past
        if (testDateTime.isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Test date cannot be in the past");
        }

        // Get price based on test type
        Double price = getTestPrice(request.getTestType());

        BloodTest bloodTest = new BloodTest();
        bloodTest.setPatient(currentUser);
        bloodTest.setClinic(clinic);
        bloodTest.setTestType(request.getTestType());
        bloodTest.setTestDate(testDateTime);
        bloodTest.setTestTime(testTime);
        bloodTest.setStatus("PENDING");
        bloodTest.setPrice(price);
        bloodTest.setNotes(request.getNotes());

        BloodTest saved = bloodTestRepository.save(bloodTest);
        return convertToResponse(saved);
    }

    public BloodTestResponse getBloodTestById(Long id) {
        BloodTest bloodTest = bloodTestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Blood test not found with id: " + id));
        
        User currentUser = authService.getCurrentUser();
        
        // Patient can only see their own tests, ADMIN and DOCTOR can see all
        if ("PATIENT".equals(currentUser.getRole()) && !bloodTest.getPatient().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You can only view your own blood tests");
        }
        
        return convertToResponse(bloodTest);
    }

    public List<BloodTestResponse> getMyBloodTests() {
        User currentUser = authService.getCurrentUser();
        List<BloodTest> tests = bloodTestRepository.findByPatientId(currentUser.getId());
        return tests.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<BloodTestResponse> getAllBloodTests(String status) {
        User currentUser = authService.getCurrentUser();
        
        // Only ADMIN and DOCTOR can view all tests
        if (!"ADMIN".equals(currentUser.getRole()) && !"DOCTOR".equals(currentUser.getRole())) {
            throw new ForbiddenException("Only ADMIN and DOCTOR can view all blood tests");
        }

        List<BloodTest> tests;
        if (status != null && !status.isEmpty()) {
            tests = bloodTestRepository.findByStatus(status);
        } else {
            tests = bloodTestRepository.findAll();
        }
        
        return tests.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<BloodTestResponse> getBloodTestsByClinic(Long clinicId, String status) {
        User currentUser = authService.getCurrentUser();
        
        // Only ADMIN and DOCTOR can view tests by clinic
        if (!"ADMIN".equals(currentUser.getRole()) && !"DOCTOR".equals(currentUser.getRole())) {
            throw new ForbiddenException("Only ADMIN and DOCTOR can view blood tests by clinic");
        }

        clinicRepository.findById(clinicId)
                .orElseThrow(() -> new NotFoundException("Clinic not found with id: " + clinicId));

        List<BloodTest> tests;
        if (status != null && !status.isEmpty()) {
            tests = bloodTestRepository.findByClinicIdAndStatus(clinicId, status);
        } else {
            tests = bloodTestRepository.findByClinicId(clinicId);
        }
        
        return tests.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public BloodTestResponse updateBloodTestStatus(Long id, String status) {
        BloodTest bloodTest = bloodTestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Blood test not found with id: " + id));

        User currentUser = authService.getCurrentUser();
        
        // Only ADMIN and DOCTOR can update status
        if (!"ADMIN".equals(currentUser.getRole()) && !"DOCTOR".equals(currentUser.getRole())) {
            throw new ForbiddenException("Only ADMIN and DOCTOR can update blood test status");
        }

        // Validate status
        if (!"PENDING".equals(status) && !"SCHEDULED".equals(status) && 
            !"COMPLETED".equals(status) && !"CANCELLED".equals(status)) {
            throw new BadRequestException("Invalid status. Must be PENDING, SCHEDULED, COMPLETED, or CANCELLED");
        }

        bloodTest.setStatus(status);
        
        if ("COMPLETED".equals(status)) {
            bloodTest.setCompletedAt(LocalDateTime.now());
        }

        BloodTest updated = bloodTestRepository.save(bloodTest);
        return convertToResponse(updated);
    }

    public BloodTestResponse updateBloodTestResult(Long id, String resultFileUrl) {
        BloodTest bloodTest = bloodTestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Blood test not found with id: " + id));

        User currentUser = authService.getCurrentUser();
        
        // Only ADMIN and DOCTOR can update results
        if (!"ADMIN".equals(currentUser.getRole()) && !"DOCTOR".equals(currentUser.getRole())) {
            throw new ForbiddenException("Only ADMIN and DOCTOR can update blood test results");
        }

        bloodTest.setResultFileUrl(resultFileUrl);
        if (resultFileUrl != null && !resultFileUrl.isEmpty()) {
            bloodTest.setStatus("COMPLETED");
            bloodTest.setCompletedAt(LocalDateTime.now());
        }

        BloodTest updated = bloodTestRepository.save(bloodTest);
        return convertToResponse(updated);
    }

    private Double getTestPrice(String testType) {
        return switch (testType.toUpperCase()) {
            case "COMPLETE BLOOD COUNT (CBC)", "CBC" -> CBC_PRICE;
            case "BLOOD GLUCOSE TEST", "BLOOD GLUCOSE" -> BLOOD_GLUCOSE_PRICE;
            case "LIPID PANEL" -> LIPID_PANEL_PRICE;
            case "LIVER FUNCTION TEST", "LIVER FUNCTION" -> LIVER_FUNCTION_PRICE;
            case "THYROID FUNCTION TEST", "THYROID FUNCTION" -> THYROID_FUNCTION_PRICE;
            case "VITAMIN D TEST", "VITAMIN D" -> VITAMIN_D_PRICE;
            default -> 50.0; // Default price
        };
    }

    private BloodTestResponse convertToResponse(BloodTest bloodTest) {
        BloodTestResponse response = new BloodTestResponse();
        response.setId(bloodTest.getId());
        response.setPatientId(bloodTest.getPatient().getId());
        response.setPatientName(bloodTest.getPatient().getFullName());
        response.setClinicId(bloodTest.getClinic().getId());
        response.setClinicName(bloodTest.getClinic().getName());
        response.setTestType(bloodTest.getTestType());
        response.setTestDate(bloodTest.getTestDate());
        response.setTestTime(bloodTest.getTestTime());
        response.setStatus(bloodTest.getStatus());
        response.setResultFileUrl(bloodTest.getResultFileUrl());
        response.setNotes(bloodTest.getNotes());
        response.setPrice(bloodTest.getPrice());
        response.setCreatedAt(bloodTest.getCreatedAt());
        response.setCompletedAt(bloodTest.getCompletedAt());
        return response;
    }
}




