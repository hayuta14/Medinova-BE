package com.project.medinova.service;

import com.project.medinova.dto.CreateSurgeryConsultationRequest;
import com.project.medinova.dto.SurgeryConsultationResponse;
import com.project.medinova.entity.*;
import com.project.medinova.exception.BadRequestException;
import com.project.medinova.exception.ForbiddenException;
import com.project.medinova.exception.NotFoundException;
import com.project.medinova.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SurgeryService {

    @Autowired
    private SurgeryConsultationRepository consultationRepository;

    @Autowired
    private ClinicRepository clinicRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private AuthService authService;

    public SurgeryConsultationResponse createSurgeryConsultation(CreateSurgeryConsultationRequest request) {
        User currentUser = authService.getCurrentUser();
        
        // Only PATIENT can create surgery consultation requests
        if (!"PATIENT".equals(currentUser.getRole())) {
            throw new ForbiddenException("Only patients can create surgery consultation requests");
        }

        Clinic clinic = clinicRepository.findById(request.getClinicId())
                .orElseThrow(() -> new NotFoundException("Clinic not found with id: " + request.getClinicId()));

        // Validate urgency
        String urgency = request.getUrgency() != null ? request.getUrgency().toUpperCase() : "ROUTINE";
        if (!"ROUTINE".equals(urgency) && !"URGENT".equals(urgency) && !"EMERGENCY".equals(urgency)) {
            throw new BadRequestException("Invalid urgency. Must be ROUTINE, URGENT, or EMERGENCY");
        }

        SurgeryConsultation consultation = new SurgeryConsultation();
        consultation.setPatient(currentUser);
        consultation.setClinic(clinic);
        consultation.setSurgeryType(request.getSurgeryType());
        consultation.setDescription(request.getDescription());
        consultation.setUrgency(urgency);
        consultation.setStatus("PENDING");

        SurgeryConsultation saved = consultationRepository.save(consultation);
        return convertToResponse(saved);
    }

    public SurgeryConsultationResponse getSurgeryConsultationById(Long id) {
        SurgeryConsultation consultation = consultationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Surgery consultation not found with id: " + id));

        User currentUser = authService.getCurrentUser();
        
        // Patient can only see their own consultations, ADMIN and DOCTOR can see all
        if ("PATIENT".equals(currentUser.getRole()) && !consultation.getPatient().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You can only view your own surgery consultations");
        }

        return convertToResponse(consultation);
    }

    public List<SurgeryConsultationResponse> getMySurgeryConsultations() {
        User currentUser = authService.getCurrentUser();
        List<SurgeryConsultation> consultations = consultationRepository.findByPatientId(currentUser.getId());
        return consultations.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<SurgeryConsultationResponse> getAllSurgeryConsultations(String status) {
        User currentUser = authService.getCurrentUser();
        
        // Only ADMIN and DOCTOR can view all consultations
        if (!"ADMIN".equals(currentUser.getRole()) && !"DOCTOR".equals(currentUser.getRole())) {
            throw new ForbiddenException("Only ADMIN and DOCTOR can view all surgery consultations");
        }

        List<SurgeryConsultation> consultations;
        if (status != null && !status.isEmpty()) {
            consultations = consultationRepository.findByStatus(status);
        } else {
            consultations = consultationRepository.findAll();
        }

        return consultations.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<SurgeryConsultationResponse> getSurgeryConsultationsByDoctor(Long doctorId, String status) {
        User currentUser = authService.getCurrentUser();
        
        // Only ADMIN and DOCTOR can view consultations by doctor
        if (!"ADMIN".equals(currentUser.getRole()) && !"DOCTOR".equals(currentUser.getRole())) {
            throw new ForbiddenException("Only ADMIN and DOCTOR can view surgery consultations by doctor");
        }

        doctorRepository.findById(doctorId)
                .orElseThrow(() -> new NotFoundException("Doctor not found with id: " + doctorId));

        List<SurgeryConsultation> consultations;
        if (status != null && !status.isEmpty()) {
            consultations = consultationRepository.findByDoctorIdAndStatus(doctorId, status);
        } else {
            consultations = consultationRepository.findByDoctorId(doctorId);
        }

        return consultations.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public SurgeryConsultationResponse assignDoctor(Long id, Long doctorId) {
        SurgeryConsultation consultation = consultationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Surgery consultation not found with id: " + id));

        User currentUser = authService.getCurrentUser();
        
        // Only ADMIN and DOCTOR can assign doctors
        if (!"ADMIN".equals(currentUser.getRole()) && !"DOCTOR".equals(currentUser.getRole())) {
            throw new ForbiddenException("Only ADMIN and DOCTOR can assign doctors to consultations");
        }

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new NotFoundException("Doctor not found with id: " + doctorId));

        consultation.setDoctor(doctor);
        consultation.setStatus("CONSULTATION_SCHEDULED");

        SurgeryConsultation updated = consultationRepository.save(consultation);
        return convertToResponse(updated);
    }

    public SurgeryConsultationResponse updateSurgeryConsultationStatus(Long id, String status) {
        SurgeryConsultation consultation = consultationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Surgery consultation not found with id: " + id));

        User currentUser = authService.getCurrentUser();
        
        // Only ADMIN and DOCTOR can update status
        if (!"ADMIN".equals(currentUser.getRole()) && !"DOCTOR".equals(currentUser.getRole())) {
            throw new ForbiddenException("Only ADMIN and DOCTOR can update surgery consultation status");
        }

        // Validate status
        if (!"PENDING".equals(status) && !"CONSULTATION_SCHEDULED".equals(status) && 
            !"CONSULTATION_COMPLETED".equals(status) && !"SURGERY_SCHEDULED".equals(status) && 
            !"SURGERY_COMPLETED".equals(status) && !"CANCELLED".equals(status)) {
            throw new BadRequestException("Invalid status");
        }

        consultation.setStatus(status);

        SurgeryConsultation updated = consultationRepository.save(consultation);
        return convertToResponse(updated);
    }

    public SurgeryConsultationResponse updateSurgeryConsultationNotes(Long id, String notes) {
        SurgeryConsultation consultation = consultationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Surgery consultation not found with id: " + id));

        User currentUser = authService.getCurrentUser();
        
        // Only ADMIN and DOCTOR can update notes
        if (!"ADMIN".equals(currentUser.getRole()) && !"DOCTOR".equals(currentUser.getRole())) {
            throw new ForbiddenException("Only ADMIN and DOCTOR can update surgery consultation notes");
        }

        consultation.setNotes(notes);

        SurgeryConsultation updated = consultationRepository.save(consultation);
        return convertToResponse(updated);
    }

    private SurgeryConsultationResponse convertToResponse(SurgeryConsultation consultation) {
        SurgeryConsultationResponse response = new SurgeryConsultationResponse();
        response.setId(consultation.getId());
        response.setPatientId(consultation.getPatient().getId());
        response.setPatientName(consultation.getPatient().getFullName());
        response.setDoctorId(consultation.getDoctor() != null ? consultation.getDoctor().getId() : null);
        response.setDoctorName(consultation.getDoctor() != null ? consultation.getDoctor().getUser().getFullName() : null);
        response.setClinicId(consultation.getClinic().getId());
        response.setClinicName(consultation.getClinic().getName());
        response.setSurgeryType(consultation.getSurgeryType());
        response.setDescription(consultation.getDescription());
        response.setUrgency(consultation.getUrgency());
        response.setStatus(consultation.getStatus());
        response.setConsultationAppointmentId(consultation.getConsultationAppointmentId());
        response.setSurgeryAppointmentId(consultation.getSurgeryAppointmentId());
        response.setNotes(consultation.getNotes());
        response.setCreatedAt(consultation.getCreatedAt());
        response.setConsultationDate(consultation.getConsultationDate());
        response.setSurgeryDate(consultation.getSurgeryDate());
        return response;
    }
}



