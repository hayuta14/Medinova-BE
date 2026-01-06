package com.project.medinova.service;

import com.project.medinova.dto.AmbulanceBookingResponse;
import com.project.medinova.dto.CreateAmbulanceBookingRequest;
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
public class AmbulanceBookingService {

    @Autowired
    private AmbulanceBookingRepository bookingRepository;

    @Autowired
    private AmbulanceRepository ambulanceRepository;

    @Autowired
    private ClinicRepository clinicRepository;

    @Autowired
    private AuthService authService;

    // Haversine formula to calculate distance
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    public AmbulanceBookingResponse createAmbulanceBooking(CreateAmbulanceBookingRequest request) {
        User currentUser = authService.getCurrentUser();
        
        // PATIENT, DOCTOR, ADMIN can create bookings (or guest if not authenticated - but we require auth)
        if (currentUser == null) {
            throw new ForbiddenException("Authentication required");
        }

        Clinic clinic = clinicRepository.findById(request.getClinicId())
                .orElseThrow(() -> new NotFoundException("Clinic not found with id: " + request.getClinicId()));

        // Find nearest available ambulance
        Ambulance assignedAmbulance = findNearestAvailableAmbulance(
                request.getPickupLat(), request.getPickupLng(), clinic.getId());

        AmbulanceBooking booking = new AmbulanceBooking();
        booking.setPatient(currentUser);
        booking.setAmbulance(assignedAmbulance);
        booking.setClinic(clinic);
        booking.setPickupLat(request.getPickupLat());
        booking.setPickupLng(request.getPickupLng());
        booking.setPickupAddress(request.getPickupAddress());
        booking.setDestinationLat(request.getDestinationLat());
        booking.setDestinationLng(request.getDestinationLng());
        booking.setDestinationAddress(request.getDestinationAddress());
        booking.setPatientName(request.getPatientName());
        booking.setPatientPhone(request.getPatientPhone());
        booking.setStatus(assignedAmbulance != null ? "ASSIGNED" : "PENDING");
        booking.setNotes(request.getNotes());

        // Calculate distance and estimated time
        if (request.getDestinationLat() != null && request.getDestinationLng() != null) {
            double distance = calculateDistance(
                    request.getPickupLat(), request.getPickupLng(),
                    request.getDestinationLat(), request.getDestinationLng());
            booking.setDistanceKm(distance);
            booking.setEstimatedTime((int) (distance * 2)); // Assume 30 km/h average speed
        } else {
            booking.setEstimatedTime(15); // Default 15 minutes if no destination
        }

        if (assignedAmbulance != null) {
            booking.setAssignedAt(java.time.LocalDateTime.now());
            // Update ambulance status
            assignedAmbulance.setStatus("DISPATCHED");
            ambulanceRepository.save(assignedAmbulance);
        }

        AmbulanceBooking saved = bookingRepository.save(booking);
        return convertToResponse(saved);
    }

    public AmbulanceBookingResponse getAmbulanceBookingById(Long id) {
        AmbulanceBooking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ambulance booking not found with id: " + id));

        User currentUser = authService.getCurrentUser();
        
        // Patient can only see their own bookings, ADMIN and DOCTOR can see all
        if ("PATIENT".equals(currentUser.getRole()) && 
            booking.getPatient() != null && 
            !booking.getPatient().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You can only view your own ambulance bookings");
        }

        return convertToResponse(booking);
    }

    public List<AmbulanceBookingResponse> getMyAmbulanceBookings() {
        User currentUser = authService.getCurrentUser();
        List<AmbulanceBooking> bookings = bookingRepository.findByPatientId(currentUser.getId());
        return bookings.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<AmbulanceBookingResponse> getAllAmbulanceBookings(String status) {
        User currentUser = authService.getCurrentUser();
        
        // Only ADMIN and DOCTOR can view all bookings
        if (!"ADMIN".equals(currentUser.getRole()) && !"DOCTOR".equals(currentUser.getRole())) {
            throw new ForbiddenException("Only ADMIN and DOCTOR can view all ambulance bookings");
        }

        List<AmbulanceBooking> bookings;
        if (status != null && !status.isEmpty()) {
            bookings = bookingRepository.findByStatus(status);
        } else {
            bookings = bookingRepository.findAll();
        }

        return bookings.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<AmbulanceBookingResponse> getAmbulanceBookingsByAmbulance(Long ambulanceId, String status) {
        User currentUser = authService.getCurrentUser();
        
        // Only ADMIN and DOCTOR can view bookings by ambulance
        if (!"ADMIN".equals(currentUser.getRole()) && !"DOCTOR".equals(currentUser.getRole())) {
            throw new ForbiddenException("Only ADMIN and DOCTOR can view ambulance bookings by ambulance");
        }

        ambulanceRepository.findById(ambulanceId)
                .orElseThrow(() -> new NotFoundException("Ambulance not found with id: " + ambulanceId));

        List<AmbulanceBooking> bookings;
        if (status != null && !status.isEmpty()) {
            bookings = bookingRepository.findByAmbulanceIdAndStatus(ambulanceId, status);
        } else {
            bookings = bookingRepository.findByAmbulanceId(ambulanceId);
        }

        return bookings.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public AmbulanceBookingResponse updateAmbulanceBookingStatus(Long id, String status) {
        AmbulanceBooking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ambulance booking not found with id: " + id));

        User currentUser = authService.getCurrentUser();
        
        // Only ADMIN and DOCTOR can update status
        if (!"ADMIN".equals(currentUser.getRole()) && !"DOCTOR".equals(currentUser.getRole())) {
            throw new ForbiddenException("Only ADMIN and DOCTOR can update ambulance booking status");
        }

        // Validate status
        if (!"PENDING".equals(status) && !"ASSIGNED".equals(status) && 
            !"IN_TRANSIT".equals(status) && !"ARRIVED".equals(status) && 
            !"COMPLETED".equals(status) && !"CANCELLED".equals(status)) {
            throw new BadRequestException("Invalid status");
        }

        String oldStatus = booking.getStatus();
        booking.setStatus(status);

        // Update timestamps
        if ("ASSIGNED".equals(status) && !"ASSIGNED".equals(oldStatus)) {
            booking.setAssignedAt(java.time.LocalDateTime.now());
            if (booking.getAmbulance() != null) {
                booking.getAmbulance().setStatus("DISPATCHED");
                ambulanceRepository.save(booking.getAmbulance());
            }
        } else if ("ARRIVED".equals(status)) {
            booking.setArrivedAt(java.time.LocalDateTime.now());
        } else if ("COMPLETED".equals(status)) {
            booking.setCompletedAt(java.time.LocalDateTime.now());
            // Release ambulance
            if (booking.getAmbulance() != null) {
                booking.getAmbulance().setStatus("AVAILABLE");
                ambulanceRepository.save(booking.getAmbulance());
            }
        } else if ("CANCELLED".equals(status)) {
            // Release ambulance if assigned
            if (booking.getAmbulance() != null && !"COMPLETED".equals(oldStatus)) {
                booking.getAmbulance().setStatus("AVAILABLE");
                ambulanceRepository.save(booking.getAmbulance());
            }
        }

        AmbulanceBooking updated = bookingRepository.save(booking);
        return convertToResponse(updated);
    }

    public AmbulanceBookingResponse assignAmbulance(Long id, Long ambulanceId) {
        AmbulanceBooking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ambulance booking not found with id: " + id));

        User currentUser = authService.getCurrentUser();
        
        // Only ADMIN and DOCTOR can assign ambulances
        if (!"ADMIN".equals(currentUser.getRole()) && !"DOCTOR".equals(currentUser.getRole())) {
            throw new ForbiddenException("Only ADMIN and DOCTOR can assign ambulances");
        }

        Ambulance ambulance = ambulanceRepository.findById(ambulanceId)
                .orElseThrow(() -> new NotFoundException("Ambulance not found with id: " + ambulanceId));

        // Check if ambulance is available
        if (!"AVAILABLE".equals(ambulance.getStatus())) {
            throw new BadRequestException("Ambulance is not available");
        }

        // Release old ambulance if exists
        if (booking.getAmbulance() != null) {
            booking.getAmbulance().setStatus("AVAILABLE");
            ambulanceRepository.save(booking.getAmbulance());
        }

        booking.setAmbulance(ambulance);
        booking.setStatus("ASSIGNED");
        booking.setAssignedAt(java.time.LocalDateTime.now());

        // Update ambulance status
        ambulance.setStatus("DISPATCHED");
        ambulanceRepository.save(ambulance);

        AmbulanceBooking updated = bookingRepository.save(booking);
        return convertToResponse(updated);
    }

    private Ambulance findNearestAvailableAmbulance(double pickupLat, double pickupLng, Long clinicId) {
        List<Ambulance> availableAmbulances = ambulanceRepository.findAll().stream()
                .filter(a -> "AVAILABLE".equals(a.getStatus()))
                .filter(a -> a.getClinic().getId().equals(clinicId))
                .collect(Collectors.toList());

        if (availableAmbulances.isEmpty()) {
            return null;
        }

        // Find nearest ambulance
        Ambulance nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (Ambulance ambulance : availableAmbulances) {
            if (ambulance.getCurrentLat() != null && ambulance.getCurrentLng() != null) {
                double distance = calculateDistance(
                        pickupLat, pickupLng,
                        ambulance.getCurrentLat(), ambulance.getCurrentLng());
                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = ambulance;
                }
            }
        }

        return nearest;
    }

    private AmbulanceBookingResponse convertToResponse(AmbulanceBooking booking) {
        AmbulanceBookingResponse response = new AmbulanceBookingResponse();
        response.setId(booking.getId());
        response.setPatientId(booking.getPatient() != null ? booking.getPatient().getId() : null);
        response.setPatientName(booking.getPatientName() != null ? booking.getPatientName() : 
                               (booking.getPatient() != null ? booking.getPatient().getFullName() : null));
        response.setAmbulanceId(booking.getAmbulance() != null ? booking.getAmbulance().getId() : null);
        response.setAmbulanceLicensePlate(booking.getAmbulance() != null ? booking.getAmbulance().getLicensePlate() : null);
        response.setClinicId(booking.getClinic().getId());
        response.setClinicName(booking.getClinic().getName());
        response.setPickupLat(booking.getPickupLat());
        response.setPickupLng(booking.getPickupLng());
        response.setPickupAddress(booking.getPickupAddress());
        response.setDestinationLat(booking.getDestinationLat());
        response.setDestinationLng(booking.getDestinationLng());
        response.setDestinationAddress(booking.getDestinationAddress());
        response.setStatus(booking.getStatus());
        response.setEstimatedTime(booking.getEstimatedTime());
        response.setDistanceKm(booking.getDistanceKm());
        response.setNotes(booking.getNotes());
        response.setCreatedAt(booking.getCreatedAt());
        response.setAssignedAt(booking.getAssignedAt());
        response.setArrivedAt(booking.getArrivedAt());
        response.setCompletedAt(booking.getCompletedAt());
        return response;
    }
}




