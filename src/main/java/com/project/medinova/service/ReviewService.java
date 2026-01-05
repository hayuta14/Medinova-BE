package com.project.medinova.service;

import com.project.medinova.dto.CreateReviewRequest;
import com.project.medinova.dto.ReviewResponse;
import com.project.medinova.entity.Appointment;
import com.project.medinova.entity.Doctor;
import com.project.medinova.entity.DoctorReview;
import com.project.medinova.entity.User;
import com.project.medinova.exception.BadRequestException;
import com.project.medinova.exception.ForbiddenException;
import com.project.medinova.exception.NotFoundException;
import com.project.medinova.repository.AppointmentRepository;
import com.project.medinova.repository.DoctorRepository;
import com.project.medinova.repository.DoctorReviewRepository;
import com.project.medinova.repository.DoctorScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReviewService {

    @Autowired
    private DoctorReviewRepository reviewRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private DoctorScheduleRepository scheduleRepository;

    @Autowired
    private AuthService authService;

    public ReviewResponse createReview(CreateReviewRequest request) {
        User currentUser = authService.getCurrentUser();
        
        // Only PATIENT can create reviews
        if (!"PATIENT".equals(currentUser.getRole())) {
            throw new ForbiddenException("Only patients can create reviews");
        }

        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new NotFoundException("Doctor not found with id: " + request.getDoctorId()));

        Appointment appointment = null;
        
        // If appointmentId is provided, validate appointment
        if (request.getAppointmentId() != null) {
            appointment = appointmentRepository.findById(request.getAppointmentId())
                    .orElseThrow(() -> new NotFoundException("Appointment not found with id: " + request.getAppointmentId()));

            // Check appointment belongs to current patient
            if (!appointment.getPatient().getId().equals(currentUser.getId())) {
                throw new ForbiddenException("You can only review doctors from your own appointments");
            }

            // Check appointment doctor matches request doctor
            if (!appointment.getDoctor().getId().equals(doctor.getId())) {
                throw new BadRequestException("Appointment doctor does not match the review doctor");
            }

            // Check appointment status - must be REVIEW or COMPLETED
            String appointmentStatus = appointment.getStatus();
            if (!"REVIEW".equals(appointmentStatus) && !"COMPLETED".equals(appointmentStatus)) {
                throw new BadRequestException("You can only review doctors after appointment status is REVIEW or COMPLETED. Current status: " + appointmentStatus);
            }

            // Check if patient already reviewed this appointment
            DoctorReview existingReview = reviewRepository.findByAppointmentIdAndPatientId(
                    request.getAppointmentId(), currentUser.getId());
            if (existingReview != null) {
                throw new BadRequestException("You have already reviewed this appointment");
            }
        } else {
            // If no appointmentId, check if patient already reviewed this doctor (general review)
            List<DoctorReview> existingReviews = reviewRepository.findByDoctorIdAndPatientId(
                    request.getDoctorId(), currentUser.getId());
            // Only block if there's a review without appointment (general review)
            boolean hasGeneralReview = existingReviews.stream()
                    .anyMatch(r -> r.getAppointment() == null);
            if (hasGeneralReview) {
                throw new BadRequestException("You have already reviewed this doctor. Please review through an appointment.");
            }
        }

        DoctorReview review = new DoctorReview();
        review.setDoctor(doctor);
        review.setPatient(currentUser);
        review.setAppointment(appointment);
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        DoctorReview savedReview = reviewRepository.save(review);
        
        // Nếu review có appointment và appointment status là REVIEW, tự động chuyển sang COMPLETED
        if (appointment != null && "REVIEW".equals(appointment.getStatus())) {
            appointment.setStatus("COMPLETED");
            appointmentRepository.save(appointment);
            
            // Cập nhật schedule status nếu có
            if (appointment.getSchedule() != null) {
                appointment.getSchedule().setStatus("COMPLETED");
                scheduleRepository.save(appointment.getSchedule());
            }
        }
        
        return convertToResponse(savedReview);
    }

    public ReviewResponse getReviewById(Long id) {
        DoctorReview review = reviewRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Review not found with id: " + id));
        return convertToResponse(review);
    }

    public List<ReviewResponse> getReviewsByDoctor(Long doctorId) {
        doctorRepository.findById(doctorId)
                .orElseThrow(() -> new NotFoundException("Doctor not found with id: " + doctorId));
        
        List<DoctorReview> reviews = reviewRepository.findByDoctorId(doctorId);
        return reviews.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<ReviewResponse> getMyReviews() {
        User currentUser = authService.getCurrentUser();
        List<DoctorReview> reviews = reviewRepository.findByPatientId(currentUser.getId());
        return reviews.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public void deleteReview(Long id) {
        DoctorReview review = reviewRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Review not found with id: " + id));

        User currentUser = authService.getCurrentUser();
        
        // Only author or ADMIN can delete
        if (!review.getPatient().getId().equals(currentUser.getId()) && !"ADMIN".equals(currentUser.getRole())) {
            throw new ForbiddenException("You can only delete your own reviews");
        }

        reviewRepository.delete(review);
    }

    private ReviewResponse convertToResponse(DoctorReview review) {
        ReviewResponse response = new ReviewResponse();
        response.setId(review.getId());
        response.setDoctorId(review.getDoctor().getId());
        response.setDoctorName(review.getDoctor().getUser().getFullName());
        response.setPatientId(review.getPatient().getId());
        response.setPatientName(review.getPatient().getFullName());
        if (review.getAppointment() != null) {
            response.setAppointmentId(review.getAppointment().getId());
        }
        response.setRating(review.getRating());
        response.setComment(review.getComment());
        response.setCreatedAt(review.getCreatedAt());
        return response;
    }
}

