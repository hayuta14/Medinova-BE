package com.project.medinova.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserProfileRequest {
    @Size(max = 255, message = "Full name must not exceed 255 characters")
    private String fullName;

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;

    private String gender;

    private String dateOfBirth;

    private String address;

    private String avatarUrl;

    // Thông tin tiểu sử bệnh
    private PatientMedicalHistoryRequest medicalHistory;
}

