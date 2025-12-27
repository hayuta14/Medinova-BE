package com.project.medinova.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientMedicalHistoryRequest {
    private String medicalCondition; // Tên bệnh/tình trạng sức khỏe

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate diagnosisDate; // Ngày chẩn đoán

    private String treatmentDescription; // Mô tả điều trị

    private String medications; // Thuốc đang sử dụng

    private String allergies; // Dị ứng

    private String chronicDiseases; // Bệnh mãn tính

    private String previousSurgeries; // Phẫu thuật trước đó

    private String familyHistory; // Tiền sử gia đình

    private String notes; // Ghi chú thêm
}

