package com.project.medinova.service;

import com.project.medinova.dto.CreateLeaveRequestRequest;
import com.project.medinova.dto.UpdateLeaveRequestStatusRequest;
import com.project.medinova.entity.Doctor;
import com.project.medinova.entity.DoctorLeaveRequest;
import com.project.medinova.entity.User;
import com.project.medinova.exception.BadRequestException;
import com.project.medinova.exception.ForbiddenException;
import com.project.medinova.exception.NotFoundException;
import com.project.medinova.repository.DoctorLeaveRequestRepository;
import com.project.medinova.repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        // Kiểm tra xem có request nào đang pending trong khoảng thời gian này không
        List<DoctorLeaveRequest> overlappingRequests = leaveRequestRepository
                .findByStartDateLessThanEqualAndEndDateGreaterThanEqualAndStatus(
                        request.getEndDate(), request.getStartDate(), "PENDING");
        
        if (!overlappingRequests.isEmpty()) {
            throw new BadRequestException("You already have a pending leave request for this period");
        }

        // Tạo leave request
        DoctorLeaveRequest leaveRequest = new DoctorLeaveRequest();
        leaveRequest.setDoctor(doctor);
        leaveRequest.setStartDate(request.getStartDate());
        leaveRequest.setEndDate(request.getEndDate());
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

