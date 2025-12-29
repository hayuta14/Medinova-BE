package com.project.medinova.service;

import com.project.medinova.dto.CreateClinicRequest;
import com.project.medinova.dto.UpdateClinicRequest;
import com.project.medinova.entity.Clinic;
import com.project.medinova.exception.BadRequestException;
import com.project.medinova.exception.NotFoundException;
import com.project.medinova.repository.AppointmentRepository;
import com.project.medinova.repository.ClinicRepository;
import com.project.medinova.repository.DoctorRepository;
import com.project.medinova.repository.DoctorScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ClinicService {

    @Autowired
    private ClinicRepository clinicRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private DoctorScheduleRepository doctorScheduleRepository;

    public Clinic createClinic(CreateClinicRequest request) {
        Clinic clinic = new Clinic();
        clinic.setName(request.getName());
        clinic.setAddress(request.getAddress());
        clinic.setPhone(request.getPhone());
        clinic.setDescription(request.getDescription());

        return clinicRepository.save(clinic);
    }

    public Clinic getClinicById(Long id) {
        return clinicRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Clinic not found with id: " + id));
    }

    public List<Clinic> getAllClinics() {
        return clinicRepository.findAll();
    }

    public Page<Clinic> getAllClinics(Pageable pageable) {
        return clinicRepository.findAll(pageable);
    }

    public Clinic updateClinic(Long id, UpdateClinicRequest request) {
        Clinic clinic = clinicRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Clinic not found with id: " + id));

        if (request.getName() != null) {
            clinic.setName(request.getName());
        }
        if (request.getAddress() != null) {
            clinic.setAddress(request.getAddress());
        }
        if (request.getPhone() != null) {
            clinic.setPhone(request.getPhone());
        }
        if (request.getDescription() != null) {
            clinic.setDescription(request.getDescription());
        }

        return clinicRepository.save(clinic);
    }

    public void deleteClinic(Long id) {
        Clinic clinic = clinicRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Clinic not found with id: " + id));

        // Kiểm tra xem clinic có doctor phụ thuộc không
        List<com.project.medinova.entity.Doctor> doctors = doctorRepository.findByClinicId(id);
        if (!doctors.isEmpty()) {
            throw new BadRequestException("Cannot delete clinic. There are " + doctors.size() + " doctor(s) associated with this clinic. Please reassign or remove doctors first.");
        }

        // Kiểm tra xem clinic có appointment phụ thuộc không
        List<com.project.medinova.entity.Appointment> appointments = appointmentRepository.findByClinicId(id);
        if (!appointments.isEmpty()) {
            throw new BadRequestException("Cannot delete clinic. There are " + appointments.size() + " appointment(s) associated with this clinic. Please handle appointments first.");
        }

        // Kiểm tra xem clinic có doctor schedule phụ thuộc không
        List<com.project.medinova.entity.DoctorSchedule> schedules = doctorScheduleRepository.findByClinicId(id);
        if (!schedules.isEmpty()) {
            throw new BadRequestException("Cannot delete clinic. There are " + schedules.size() + " doctor schedule(s) associated with this clinic. Please handle schedules first.");
        }

        // Nếu không có phụ thuộc nào, cho phép xóa
        clinicRepository.delete(clinic);
    }
}

