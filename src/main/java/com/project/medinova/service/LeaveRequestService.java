package com.project.medinova.service;

import com.project.medinova.dto.CreateLeaveRequestRequest;
import com.project.medinova.dto.UpdateLeaveRequestStatusRequest;
import com.project.medinova.entity.Doctor;
import com.project.medinova.entity.DoctorLeaveRequest;
import com.project.medinova.entity.User;
import com.project.medinova.exception.BadRequestException;
import com.project.medinova.exception.ForbiddenException;
import com.project.medinova.exception.NotFoundException;
import com.project.medinova.repository.AppointmentRepository;
import com.project.medinova.repository.DoctorLeaveRequestRepository;
import com.project.medinova.repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class LeaveRequestService {

    @Autowired
    private DoctorLeaveRequestRepository leaveRequestRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private AuthService authService;

    public DoctorLeaveRequest createLeaveRequest(CreateLeaveRequestRequest request) {
        // Lấy user hiện tại từ JWT
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            throw new ForbiddenException("User not authenticated");
        }

        // Kiểm tra user có role DOCTOR
        if (!"DOCTOR".equals(currentUser.getRole())) {
            throw new ForbiddenException("Only doctors can create leave requests");
        }

        // Tìm doctor record của user
        Doctor doctor = doctorRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new NotFoundException("Doctor profile not found for current user"));

        // Validate dates
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("End date must be after or equal to start date");
        }

        // Validate time nếu có
        if (request.getStartTime() != null && request.getEndTime() != null) {
            if (request.getStartTime().isAfter(request.getEndTime())) {
                throw new BadRequestException("Start time must be before or equal to end time");
            }
            // Nếu có time, startDate và endDate phải giống nhau (chỉ xin nghỉ trong ngày)
            if (!request.getStartDate().equals(request.getEndDate())) {
                throw new BadRequestException("Start time and end time can only be used when start date and end date are the same day");
            }
        }

        // Kiểm tra overlap với leave requests khác (bỏ qua REJECTED)
        List<DoctorLeaveRequest> existingLeaves = leaveRequestRepository.findByDoctorId(doctor.getId());
        for (DoctorLeaveRequest existingLeave : existingLeaves) {
            // Bỏ qua REJECTED
            if ("REJECTED".equals(existingLeave.getStatus())) {
                continue;
            }
            
            // Kiểm tra overlap về date range
            boolean dateOverlap = !request.getEndDate().isBefore(existingLeave.getStartDate()) && 
                                 !request.getStartDate().isAfter(existingLeave.getEndDate());
            
            if (dateOverlap) {
                // Nếu cả hai đều có time (partial day leave), kiểm tra time overlap
                if (request.getStartTime() != null && request.getEndTime() != null &&
                    existingLeave.getStartTime() != null && existingLeave.getEndTime() != null) {
                    // Chỉ overlap nếu cùng một ngày
                    if (request.getStartDate().equals(existingLeave.getStartDate())) {
                        boolean timeOverlap = !request.getEndTime().isBefore(existingLeave.getStartTime()) &&
                                             !request.getStartTime().isAfter(existingLeave.getEndTime());
                        if (timeOverlap) {
                            throw new BadRequestException("Không thể tạo do đã có lịch được đặt trùng với ngày đang được chọn");
                        }
                    }
                } else {
                    // Một trong hai là full day leave hoặc cả hai đều full day -> overlap
                    throw new BadRequestException("Không thể tạo do đã có lịch được đặt trùng với ngày đang được chọn");
                }
            }
        }

        // Kiểm tra overlap với appointments của doctor (trừ CANCELLED)
        List<com.project.medinova.entity.Appointment> appointments = appointmentRepository.findByDoctorId(doctor.getId());
        for (com.project.medinova.entity.Appointment appointment : appointments) {
            // Bỏ qua CANCELLED
            if ("CANCELLED".equals(appointment.getStatus())) {
                continue;
            }
            
            LocalDate appointmentDate = appointment.getAppointmentTime().toLocalDate();
            
            // Kiểm tra overlap về date range
            boolean dateOverlap = !request.getEndDate().isBefore(appointmentDate) && 
                                 !request.getStartDate().isAfter(appointmentDate);
            
            if (dateOverlap) {
                throw new BadRequestException("Không thể tạo do đã có lịch được đặt trùng với ngày đang được chọn");
            }
        }

        // Tạo leave request
        DoctorLeaveRequest leaveRequest = new DoctorLeaveRequest();
        leaveRequest.setDoctor(doctor);
        leaveRequest.setStartDate(request.getStartDate());
        leaveRequest.setEndDate(request.getEndDate());
        leaveRequest.setStartTime(request.getStartTime());
        leaveRequest.setEndTime(request.getEndTime());
        leaveRequest.setReason(request.getReason());
        leaveRequest.setStatus("PENDING");

        return leaveRequestRepository.save(leaveRequest);
    }

    public DoctorLeaveRequest updateLeaveRequestStatus(Long id, UpdateLeaveRequestStatusRequest request) {
        // Lấy user hiện tại từ JWT
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            throw new ForbiddenException("User not authenticated");
        }

        // Kiểm tra user có role ADMIN
        if (!"ADMIN".equals(currentUser.getRole())) {
            throw new ForbiddenException("Only admins can update leave request status");
        }

        // Tìm leave request
        DoctorLeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Leave request not found with id: " + id));

        // Kiểm tra status hiện tại
        if (!"PENDING".equals(leaveRequest.getStatus())) {
            throw new BadRequestException("Only pending leave requests can be updated");
        }

        // Cập nhật status
        leaveRequest.setStatus(request.getStatus());
        leaveRequest.setApprovedBy(currentUser);
        leaveRequest.setApprovedAt(LocalDateTime.now());

        return leaveRequestRepository.save(leaveRequest);
    }

    public DoctorLeaveRequest getLeaveRequestById(Long id) {
        DoctorLeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Leave request not found with id: " + id));

        // Kiểm tra quyền: Doctor chỉ có thể xem request của mình, Admin có thể xem tất cả
        User currentUser = authService.getCurrentUser();
        if (currentUser != null && "DOCTOR".equals(currentUser.getRole())) {
            if (!leaveRequest.getDoctor().getUser().getId().equals(currentUser.getId())) {
                throw new ForbiddenException("You can only view your own leave requests");
            }
        }

        return leaveRequest;
    }

    public List<DoctorLeaveRequest> getMyLeaveRequests() {
        // Lấy user hiện tại từ JWT
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            throw new ForbiddenException("User not authenticated");
        }

        // Kiểm tra user có role DOCTOR
        if (!"DOCTOR".equals(currentUser.getRole())) {
            throw new ForbiddenException("Only doctors can view their leave requests");
        }

        // Tìm doctor record của user
        Doctor doctor = doctorRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new NotFoundException("Doctor profile not found for current user"));

        return leaveRequestRepository.findByDoctorId(doctor.getId());
    }

    public List<DoctorLeaveRequest> getAllLeaveRequests() {
        // Lấy user hiện tại từ JWT
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            throw new ForbiddenException("User not authenticated");
        }

        // Kiểm tra user có role ADMIN
        if (!"ADMIN".equals(currentUser.getRole())) {
            throw new ForbiddenException("Only admins can view all leave requests");
        }

        return leaveRequestRepository.findAll();
    }
}

