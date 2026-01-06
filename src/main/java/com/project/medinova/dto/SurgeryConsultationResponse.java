package com.project.medinova.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SurgeryConsultationResponse {
    private Long id;
    private Long patientId;
    private String patientName;
    private Long doctorId;
    private String doctorName;
    private Long clinicId;
    private String clinicName;
    private String surgeryType;
    private String description;
    private String urgency;
    private String status;
    private Long consultationAppointmentId;
    private Long surgeryAppointmentId;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime consultationDate;
    private LocalDateTime surgeryDate;
}




