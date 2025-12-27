package com.project.medinova.service;

import com.project.medinova.dto.PatientMedicalHistoryRequest;
import com.project.medinova.entity.PatientMedicalHistory;
import com.project.medinova.entity.User;
import com.project.medinova.entity.UserProfile;
import com.project.medinova.exception.NotFoundException;
import com.project.medinova.repository.PatientMedicalHistoryRepository;
import com.project.medinova.repository.UserProfileRepository;
import com.project.medinova.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class UserProfileService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private PatientMedicalHistoryRepository medicalHistoryRepository;

    @Autowired
    private AuthService authService;

    public PatientMedicalHistory updateMedicalHistory(PatientMedicalHistoryRequest request) {
        // Lấy user từ JWT token
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            throw new NotFoundException("User not found");
        }

        Long userId = currentUser.getId();
        
        // Update medical history dựa trên userId từ JWT token
        updateMedicalHistoryInternal(userId, request);
        
        // Trả về medical history đã update
        return medicalHistoryRepository.findFirstByPatientIdOrderByCreatedAtDesc(userId)
                .orElseThrow(() -> new NotFoundException("Medical history not found"));
    }

    private void updateMedicalHistoryInternal(Long patientId, PatientMedicalHistoryRequest request) {
        // Find existing medical history or create new one
        Optional<PatientMedicalHistory> existingHistory = medicalHistoryRepository
                .findFirstByPatientIdOrderByCreatedAtDesc(patientId);

        PatientMedicalHistory medicalHistory;
        if (existingHistory.isPresent()) {
            medicalHistory = existingHistory.get();
        } else {
            medicalHistory = new PatientMedicalHistory();
            User user = userRepository.findById(patientId)
                    .orElseThrow(() -> new NotFoundException("User not found"));
            medicalHistory.setPatient(user);
        }

        // Update fields if provided
        if (request.getMedicalCondition() != null) {
            medicalHistory.setMedicalCondition(request.getMedicalCondition());
        }
        if (request.getDiagnosisDate() != null) {
            medicalHistory.setDiagnosisDate(request.getDiagnosisDate());
        }
        if (request.getTreatmentDescription() != null) {
            medicalHistory.setTreatmentDescription(request.getTreatmentDescription());
        }
        if (request.getMedications() != null) {
            medicalHistory.setMedications(request.getMedications());
        }
        if (request.getAllergies() != null) {
            medicalHistory.setAllergies(request.getAllergies());
        }
        if (request.getChronicDiseases() != null) {
            medicalHistory.setChronicDiseases(request.getChronicDiseases());
        }
        if (request.getPreviousSurgeries() != null) {
            medicalHistory.setPreviousSurgeries(request.getPreviousSurgeries());
        }
        if (request.getFamilyHistory() != null) {
            medicalHistory.setFamilyHistory(request.getFamilyHistory());
        }
        if (request.getNotes() != null) {
            medicalHistory.setNotes(request.getNotes());
        }

        medicalHistoryRepository.save(medicalHistory);
    }

    public UserProfile getUserProfile() {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            throw new NotFoundException("User not found");
        }

        return userProfileRepository.findById(currentUser.getId())
                .orElseGet(() -> {
                    UserProfile profile = new UserProfile();
                    profile.setUserId(currentUser.getId());
                    profile.setUser(currentUser);
                    return profile;
                });
    }

    public PatientMedicalHistory getMedicalHistory() {
        // Lấy userId từ JWT token
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            throw new NotFoundException("User not found");
        }

        Long userId = currentUser.getId();
        
        // Trả về medical history dựa trên userId từ JWT token
        return medicalHistoryRepository.findFirstByPatientIdOrderByCreatedAtDesc(userId)
                .orElse(null);
    }
}

