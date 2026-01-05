package com.project.medinova.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateAmbulanceBookingRequest {
    @NotNull(message = "Clinic ID is required")
    private Long clinicId;

    @NotNull(message = "Pickup latitude is required")
    private Double pickupLat;

    @NotNull(message = "Pickup longitude is required")
    private Double pickupLng;

    private String pickupAddress;

    private Double destinationLat;

    private Double destinationLng;

    private String destinationAddress;

    private String patientName; // For guest bookings

    private String patientPhone; // For guest bookings

    private String notes;
}



