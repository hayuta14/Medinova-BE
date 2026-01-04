package com.project.medinova.service;

import com.project.medinova.dto.DashboardStatsResponse;
import com.project.medinova.dto.DoctorDashboardStatsResponse;
import com.project.medinova.entity.Doctor;
import com.project.medinova.entity.User;
import com.project.medinova.exception.NotFoundException;
import com.project.medinova.repository.*;
import com.project.medinova.entity.Emergency;
import com.project.medinova.entity.EmergencyAssignment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class DashboardService {

    @Autowired
    private ClinicRepository clinicRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private EmergencyRepository emergencyRepository;

    @Autowired
    private DoctorLeaveRequestRepository leaveRequestRepository;

    @Autowired
    private EmergencyAssignmentRepository emergencyAssignmentRepository;

    @Autowired
    private AuthService authService;

    public DashboardStatsResponse getAdminDashboardStats() {
        DashboardStatsResponse stats = new DashboardStatsResponse();

        // Basic counts
        stats.setTotalHospitals(clinicRepository.count());
        stats.setTotalDoctors(doctorRepository.count());
        stats.setTotalPatients(userRepository.countByRole("PATIENT"));
        stats.setTotalUsers(userRepository.count());
        stats.setPendingDoctors(doctorRepository.countByStatus("PENDING"));
        stats.setPendingLeaveRequests(leaveRequestRepository.countByStatus("PENDING"));

        // Today's data
        LocalDateTime startOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        stats.setTodayAppointments(
            (long) appointmentRepository.findByAppointmentTimeBetween(startOfToday, endOfToday).size()
        );
        stats.setTodayEmergencies(
            emergencyRepository.findAll().stream()
                .filter(e -> e.getCreatedAt() != null && 
                           e.getCreatedAt().isAfter(startOfToday) && 
                           e.getCreatedAt().isBefore(endOfToday))
                .count()
        );

        // Active emergencies (not completed or cancelled)
        stats.setActiveEmergencies(
            emergencyRepository.findAll().stream()
                .filter(e -> !"COMPLETED".equals(e.getStatus()) && !"CANCELLED".equals(e.getStatus()))
                .count()
        );

        // Total counts
        stats.setTotalAppointments((long) appointmentRepository.findAll().size());
        stats.setTotalEmergencies((long) emergencyRepository.findAll().size());

        // Group by status
        stats.setAppointmentsByStatus(getAppointmentsByStatus());
        stats.setEmergenciesByStatus(getEmergenciesByStatus());
        stats.setDoctorsByStatus(getDoctorsByStatus());

        return stats;
    }

    public DoctorDashboardStatsResponse getDoctorDashboardStats() {
        User currentUser = authService.getCurrentUser();
        Doctor doctor = doctorRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new NotFoundException("Doctor profile not found for current user"));

        DoctorDashboardStatsResponse stats = new DoctorDashboardStatsResponse();

        Long doctorId = doctor.getId();

        // Appointment stats
        stats.setTotalAppointments((long) appointmentRepository.findByDoctorId(doctorId).size());
        
        LocalDateTime startOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        
        long todayAppointments = appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(
            doctorId, startOfToday, endOfToday).size();
        stats.setTodayAppointments(todayAppointments);

        LocalDateTime now = LocalDateTime.now();
        long upcomingAppointments = appointmentRepository.findByDoctorId(doctorId).stream()
            .filter(a -> a.getAppointmentTime() != null && a.getAppointmentTime().isAfter(now))
            .filter(a -> !"CANCELLED".equals(a.getStatus()) && !"COMPLETED".equals(a.getStatus()))
            .count();
        stats.setUpcomingAppointments(upcomingAppointments);

        long completedAppointments = appointmentRepository.findByDoctorIdAndStatus(doctorId, "COMPLETED").size();
        stats.setCompletedAppointments(completedAppointments);

        // Emergency stats - use EmergencyAssignmentRepository
        List<EmergencyAssignment> doctorAssignments = 
            emergencyAssignmentRepository.findByDoctorId(doctorId);
        
        stats.setTotalEmergencies((long) doctorAssignments.size());

        long activeEmergencies = doctorAssignments.stream()
            .filter(ea -> {
                Emergency e = ea.getEmergency();
                return e != null && !"COMPLETED".equals(e.getStatus()) && !"CANCELLED".equals(e.getStatus());
            })
            .count();
        stats.setActiveEmergencies(activeEmergencies);

        long todayEmergencies = doctorAssignments.stream()
            .filter(ea -> {
                Emergency e = ea.getEmergency();
                return e != null && e.getCreatedAt() != null && 
                       e.getCreatedAt().isAfter(startOfToday) && 
                       e.getCreatedAt().isBefore(endOfToday);
            })
            .count();
        stats.setTodayEmergencies(todayEmergencies);

        // Leave requests
        stats.setPendingLeaveRequests(
            leaveRequestRepository.findByDoctorId(doctorId).stream()
                .filter(lr -> "PENDING".equals(lr.getStatus()))
                .count()
        );

        // Group by status
        stats.setAppointmentsByStatus(getAppointmentsByStatusForDoctor(doctorId));
        stats.setEmergenciesByStatus(getEmergenciesByStatusForDoctor(doctorId));

        return stats;
    }

    private Map<String, Long> getAppointmentsByStatus() {
        Map<String, Long> map = new HashMap<>();
        map.put("PENDING", (long) appointmentRepository.findByStatus("PENDING").size());
        map.put("CONFIRMED", (long) appointmentRepository.findByStatus("CONFIRMED").size());
        map.put("COMPLETED", (long) appointmentRepository.findByStatus("COMPLETED").size());
        map.put("CANCELLED", (long) appointmentRepository.findByStatus("CANCELLED").size());
        return map;
    }

    private Map<String, Long> getAppointmentsByStatusForDoctor(Long doctorId) {
        Map<String, Long> map = new HashMap<>();
        map.put("PENDING", (long) appointmentRepository.findByDoctorIdAndStatus(doctorId, "PENDING").size());
        map.put("CONFIRMED", (long) appointmentRepository.findByDoctorIdAndStatus(doctorId, "CONFIRMED").size());
        map.put("COMPLETED", (long) appointmentRepository.findByDoctorIdAndStatus(doctorId, "COMPLETED").size());
        map.put("CANCELLED", (long) appointmentRepository.findByDoctorIdAndStatus(doctorId, "CANCELLED").size());
        return map;
    }

    private Map<String, Long> getEmergenciesByStatus() {
        Map<String, Long> map = new HashMap<>();
        map.put("PENDING", (long) emergencyRepository.findByStatus("PENDING").size());
        map.put("DISPATCHED", (long) emergencyRepository.findByStatus("DISPATCHED").size());
        map.put("IN_TRANSIT", (long) emergencyRepository.findByStatus("IN_TRANSIT").size());
        map.put("ARRIVED", (long) emergencyRepository.findByStatus("ARRIVED").size());
        map.put("COMPLETED", (long) emergencyRepository.findByStatus("COMPLETED").size());
        map.put("CANCELLED", (long) emergencyRepository.findByStatus("CANCELLED").size());
        return map;
    }

    private Map<String, Long> getEmergenciesByStatusForDoctor(Long doctorId) {
        Map<String, Long> map = new HashMap<>();
        // Get emergencies assigned to this doctor and group by status
        emergencyAssignmentRepository.findByDoctorId(doctorId).stream()
            .map(EmergencyAssignment::getEmergency)
            .filter(e -> e != null)
            .forEach(e -> {
                String status = e.getStatus();
                map.put(status, map.getOrDefault(status, 0L) + 1);
            });
        return map;
    }

    private Map<String, Long> getDoctorsByStatus() {
        Map<String, Long> map = new HashMap<>();
        map.put("PENDING", doctorRepository.countByStatus("PENDING"));
        map.put("APPROVED", doctorRepository.countByStatus("APPROVED"));
        map.put("REJECTED", doctorRepository.countByStatus("REJECTED"));
        return map;
    }
}

