package com.project.medinova.service;

import com.project.medinova.entity.Appointment;
import com.project.medinova.entity.DoctorSchedule;
import com.project.medinova.repository.AppointmentRepository;
import com.project.medinova.repository.DoctorScheduleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AppointmentSchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentSchedulerService.class);

    @Autowired
    private DoctorScheduleRepository scheduleRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    /**
     * Tự động release HOLD slots sau 5 phút
     * Chạy mỗi phút
     */
    @Scheduled(fixedRate = 60000) // 60 seconds = 1 minute
    @Transactional
    public void releaseExpiredHoldSlots() {
        LocalDateTime now = LocalDateTime.now();
        List<DoctorSchedule> expiredHolds = scheduleRepository.findByStatusAndHoldExpiresAtBefore("HOLD", now);
        
        for (DoctorSchedule schedule : expiredHolds) {
            logger.info("Releasing expired HOLD slot: scheduleId={}, expiredAt={}", 
                    schedule.getId(), schedule.getHoldExpiresAt());
            
            // Xóa appointment nếu có (cascade = CascadeType.ALL sẽ tự động xóa schedule)
            if (schedule.getAppointment() != null) {
                Appointment appointment = schedule.getAppointment();
                if ("PENDING".equals(appointment.getStatus())) {
                    appointmentRepository.delete(appointment);
                    logger.info("Deleted expired PENDING appointment: appointmentId={}, scheduleId={}", 
                            appointment.getId(), schedule.getId());
                } else {
                    // Nếu appointment không phải PENDING, chỉ xóa schedule
                    scheduleRepository.delete(schedule);
                    logger.info("Deleted expired HOLD schedule: scheduleId={}", schedule.getId());
                }
            } else {
                // Nếu không có appointment, chỉ xóa schedule
                scheduleRepository.delete(schedule);
                logger.info("Deleted expired HOLD schedule without appointment: scheduleId={}", schedule.getId());
            }
        }
        
        if (!expiredHolds.isEmpty()) {
            logger.info("Released {} expired HOLD slots", expiredHolds.size());
        }
    }

    /**
     * Tự động release PENDING appointments sau 3 tiếng (cho bác sĩ)
     * Chạy mỗi 10 phút
     */
    @Scheduled(fixedRate = 600000) // 10 minutes
    @Transactional
    public void releaseExpiredPendingAppointments() {
        LocalDateTime threeHoursAgo = LocalDateTime.now().minusHours(3);
        List<Appointment> expiredPending = appointmentRepository.findByStatusAndCreatedAtBefore("PENDING", threeHoursAgo);
        
        for (Appointment appointment : expiredPending) {
            logger.info("Releasing expired PENDING appointment: appointmentId={}, createdAt={}", 
                    appointment.getId(), appointment.getCreatedAt());
            
            // Xóa appointment - cascade = CascadeType.ALL sẽ tự động xóa schedule
            appointmentRepository.delete(appointment);
            logger.info("Deleted expired PENDING appointment: appointmentId={}", appointment.getId());
        }
        
        if (!expiredPending.isEmpty()) {
            logger.info("Released {} expired PENDING appointments", expiredPending.size());
        }
    }
}


