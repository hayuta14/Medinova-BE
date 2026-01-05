package com.project.medinova.service;

import com.project.medinova.dto.*;
import com.project.medinova.entity.*;
import com.project.medinova.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PublicService {

    @Autowired
    private ClinicRepository clinicRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private DoctorReviewRepository reviewRepository;

    public PublicStatsResponse getPublicStats() {
        PublicStatsResponse stats = new PublicStatsResponse();

        // Basic counts
        stats.setTotalHospitals(clinicRepository.count());
        stats.setTotalDoctors(doctorRepository.count());
        stats.setTotalPatients(userRepository.countByRole("PATIENT"));
        stats.setTotalAppointments((long) appointmentRepository.findAll().size());

        // Featured doctors (top 6 by rating)
        stats.setFeaturedDoctors(getFeaturedDoctors(6));

        // Featured clinics (top 4 by doctor count)
        stats.setFeaturedClinics(getFeaturedClinics(4));

        // Recent posts (latest 3 published)
        stats.setRecentPosts(getRecentPosts(3));

        return stats;
    }

    private List<DoctorSummary> getFeaturedDoctors(int limit) {
        List<Doctor> approvedDoctors = doctorRepository.findByStatus("APPROVED");
        
        return approvedDoctors.stream()
                .map(doctor -> {
                    DoctorSummary summary = new DoctorSummary();
                    summary.setId(doctor.getId());
                    summary.setName(doctor.getUser().getFullName());
                    if (doctor.getDepartment() != null) {
                        summary.setDepartment(doctor.getDepartment());
                        summary.setDepartmentDisplayName(doctor.getDepartment().getDisplayName());
                    }
                    summary.setExperienceYears(doctor.getExperienceYears());
                    summary.setClinicName(doctor.getClinic().getName());

                    // Calculate average rating
                    List<DoctorReview> reviews = reviewRepository.findByDoctorId(doctor.getId());
                    if (!reviews.isEmpty()) {
                        double avgRating = reviews.stream()
                                .mapToInt(DoctorReview::getRating)
                                .average()
                                .orElse(0.0);
                        summary.setAverageRating(avgRating);
                        summary.setTotalReviews((long) reviews.size());
                    } else {
                        summary.setAverageRating(0.0);
                        summary.setTotalReviews(0L);
                    }

                    return summary;
                })
                .sorted((d1, d2) -> {
                    // Sort by rating (desc), then by review count (desc)
                    int ratingCompare = Double.compare(d2.getAverageRating(), d1.getAverageRating());
                    if (ratingCompare != 0) return ratingCompare;
                    return Long.compare(d2.getTotalReviews(), d1.getTotalReviews());
                })
                .limit(limit)
                .collect(Collectors.toList());
    }

    private List<ClinicSummary> getFeaturedClinics(int limit) {
        List<Clinic> clinics = clinicRepository.findAll();
        
        return clinics.stream()
                .map(clinic -> {
                    ClinicSummary summary = new ClinicSummary();
                    summary.setId(clinic.getId());
                    summary.setName(clinic.getName());
                    summary.setAddress(clinic.getAddress());
                    
                    List<Doctor> doctors = doctorRepository.findByClinicId(clinic.getId());
                    summary.setTotalDoctors((long) doctors.size());
                    
                    List<Appointment> appointments = appointmentRepository.findByClinicId(clinic.getId());
                    summary.setTotalAppointments((long) appointments.size());
                    
                    return summary;
                })
                .sorted((c1, c2) -> {
                    // Sort by doctor count (desc), then by appointment count (desc)
                    int doctorCompare = Long.compare(c2.getTotalDoctors(), c1.getTotalDoctors());
                    if (doctorCompare != 0) return doctorCompare;
                    return Long.compare(c2.getTotalAppointments(), c1.getTotalAppointments());
                })
                .limit(limit)
                .collect(Collectors.toList());
    }

    private List<PostSummary> getRecentPosts(int limit) {
        List<Post> publishedPosts = postRepository.findByStatus("PUBLISHED");
        
        return publishedPosts.stream()
                .sorted((p1, p2) -> {
                    // Sort by created date (desc)
                    if (p1.getCreatedAt() == null && p2.getCreatedAt() == null) return 0;
                    if (p1.getCreatedAt() == null) return 1;
                    if (p2.getCreatedAt() == null) return -1;
                    return p2.getCreatedAt().compareTo(p1.getCreatedAt());
                })
                .limit(limit)
                .map(post -> {
                    PostSummary summary = new PostSummary();
                    summary.setId(post.getId());
                    summary.setTitle(post.getTitle());
                    
                    // Content preview (first 150 characters)
                    String content = post.getContent();
                    if (content != null && content.length() > 150) {
                        summary.setContentPreview(content.substring(0, 150) + "...");
                    } else {
                        summary.setContentPreview(content);
                    }
                    
                    summary.setAuthorName(post.getAuthor().getFullName());
                    summary.setCreatedAt(post.getCreatedAt());
                    return summary;
                })
                .collect(Collectors.toList());
    }
}


