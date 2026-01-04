package com.project.medinova.service;

import com.project.medinova.dto.*;
import com.project.medinova.entity.*;
import com.project.medinova.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Transactional
public class RankingService {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private ClinicRepository clinicRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private DoctorReviewRepository reviewRepository;

    public DoctorRankingResponse getDoctorRanking(int limit) {
        List<Doctor> approvedDoctors = doctorRepository.findByStatus("APPROVED");
        
        List<DoctorRankingItem> ranking = approvedDoctors.stream()
                .map(doctor -> {
                    DoctorRankingItem item = new DoctorRankingItem();
                    item.setId(doctor.getId());
                    item.setName(doctor.getUser().getFullName());
                    item.setSpecialization(doctor.getSpecialization());
                    item.setClinicName(doctor.getClinic().getName());
                    item.setExperienceYears(doctor.getExperienceYears());

                    // Calculate stats
                    List<DoctorReview> reviews = reviewRepository.findByDoctorId(doctor.getId());
                    if (!reviews.isEmpty()) {
                        double avgRating = reviews.stream()
                                .mapToInt(DoctorReview::getRating)
                                .average()
                                .orElse(0.0);
                        item.setAverageRating(avgRating);
                        item.setTotalReviews((long) reviews.size());
                    } else {
                        item.setAverageRating(0.0);
                        item.setTotalReviews(0L);
                    }

                    List<Appointment> appointments = appointmentRepository.findByDoctorId(doctor.getId());
                    item.setTotalAppointments((long) appointments.size());

                    return item;
                })
                .sorted((d1, d2) -> {
                    // Sort by: rating (desc), reviews (desc), appointments (desc)
                    int ratingCompare = Double.compare(d2.getAverageRating(), d1.getAverageRating());
                    if (ratingCompare != 0) return ratingCompare;
                    
                    int reviewCompare = Long.compare(d2.getTotalReviews(), d1.getTotalReviews());
                    if (reviewCompare != 0) return reviewCompare;
                    
                    return Long.compare(d2.getTotalAppointments(), d1.getTotalAppointments());
                })
                .limit(limit)
                .collect(Collectors.toList());

        // Set ranks
        IntStream.range(0, ranking.size())
                .forEach(i -> ranking.get(i).setRank(i + 1));

        DoctorRankingResponse response = new DoctorRankingResponse();
        response.setTopDoctors(ranking);
        response.setTotalDoctors((long) approvedDoctors.size());
        return response;
    }

    public ClinicRankingResponse getClinicRanking(int limit) {
        List<Clinic> clinics = clinicRepository.findAll();
        
        List<ClinicRankingItem> ranking = clinics.stream()
                .map(clinic -> {
                    ClinicRankingItem item = new ClinicRankingItem();
                    item.setId(clinic.getId());
                    item.setName(clinic.getName());
                    item.setAddress(clinic.getAddress());

                    List<Doctor> doctors = doctorRepository.findByClinicId(clinic.getId());
                    item.setTotalDoctors((long) doctors.size());

                    List<Appointment> appointments = appointmentRepository.findByClinicId(clinic.getId());
                    item.setTotalAppointments((long) appointments.size());

                    // Calculate average doctor rating
                    if (!doctors.isEmpty()) {
                        double totalRating = 0.0;
                        long totalReviews = 0;
                        for (Doctor doctor : doctors) {
                            List<DoctorReview> reviews = reviewRepository.findByDoctorId(doctor.getId());
                            if (!reviews.isEmpty()) {
                                double avgRating = reviews.stream()
                                        .mapToInt(DoctorReview::getRating)
                                        .average()
                                        .orElse(0.0);
                                totalRating += avgRating * reviews.size();
                                totalReviews += reviews.size();
                            }
                        }
                        item.setAverageDoctorRating(totalReviews > 0 ? totalRating / totalReviews : 0.0);
                    } else {
                        item.setAverageDoctorRating(0.0);
                    }

                    return item;
                })
                .sorted((c1, c2) -> {
                    // Sort by: appointments (desc), doctors (desc), rating (desc)
                    int appointmentCompare = Long.compare(c2.getTotalAppointments(), c1.getTotalAppointments());
                    if (appointmentCompare != 0) return appointmentCompare;
                    
                    int doctorCompare = Long.compare(c2.getTotalDoctors(), c1.getTotalDoctors());
                    if (doctorCompare != 0) return doctorCompare;
                    
                    return Double.compare(c2.getAverageDoctorRating(), c1.getAverageDoctorRating());
                })
                .limit(limit)
                .collect(Collectors.toList());

        // Set ranks
        IntStream.range(0, ranking.size())
                .forEach(i -> ranking.get(i).setRank(i + 1));

        ClinicRankingResponse response = new ClinicRankingResponse();
        response.setTopClinics(ranking);
        response.setTotalClinics((long) clinics.size());
        return response;
    }
}

