package com.project.medinova.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AmbulanceBookingResponse {
    private Long id;
    private Long patientId;
    private String patientName;
    private Long ambulanceId;
    private String ambulanceLicensePlate;
    private Long clinicId;
    private String clinicName;
    private Double pickupLat;
    private Double pickupLng;
    private String pickupAddress;
    private Double destinationLat;
    private Double destinationLng;
    private String destinationAddress;
    private String status;
    private Integer estimatedTime;
    private Double distanceKm;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime assignedAt;
    private LocalDateTime arrivedAt;
    private LocalDateTime completedAt;
}

