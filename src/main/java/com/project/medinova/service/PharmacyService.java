package com.project.medinova.service;

import com.project.medinova.dto.*;
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
public class PharmacyService {

    @Autowired
    private PharmacyOrderRepository orderRepository;

    @Autowired
    private PharmacyOrderItemRepository itemRepository;

    @Autowired
    private ClinicRepository clinicRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private AuthService authService;

    private static final double DEFAULT_DELIVERY_FEE = 5.0;

    public PharmacyOrderResponse createPharmacyOrder(CreatePharmacyOrderRequest request) {
        User currentUser = authService.getCurrentUser();
        
        // Only PATIENT can create pharmacy orders
        if (!"PATIENT".equals(currentUser.getRole())) {
            throw new ForbiddenException("Only patients can create pharmacy orders");
        }

        Clinic clinic = clinicRepository.findById(request.getClinicId())
                .orElseThrow(() -> new NotFoundException("Clinic not found with id: " + request.getClinicId()));

        // Validate appointment if provided
        Appointment appointment = null;
        if (request.getAppointmentId() != null) {
            appointment = appointmentRepository.findById(request.getAppointmentId())
                    .orElseThrow(() -> new NotFoundException("Appointment not found with id: " + request.getAppointmentId()));
            
            // Verify appointment belongs to current user
            if (!appointment.getPatient().getId().equals(currentUser.getId())) {
                throw new ForbiddenException("Appointment does not belong to current user");
            }
        }

        // Calculate total amount
        double subtotal = request.getItems().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
        double deliveryFee = DEFAULT_DELIVERY_FEE;
        double totalAmount = subtotal + deliveryFee;

        // Create order
        PharmacyOrder order = new PharmacyOrder();
        order.setPatient(currentUser);
        order.setAppointment(appointment);
        order.setClinic(clinic);
        order.setPrescriptionFileUrl(request.getPrescriptionFileUrl());
        order.setStatus("PENDING");
        order.setDeliveryAddress(request.getDeliveryAddress());
        order.setDeliveryPhone(request.getDeliveryPhone());
        order.setDeliveryName(request.getDeliveryName());
        order.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "CASH_ON_DELIVERY");
        order.setTotalAmount(totalAmount);
        order.setDeliveryFee(deliveryFee);
        order.setNotes(request.getNotes());

        PharmacyOrder savedOrder = orderRepository.save(order);

        // Create order items
        List<PharmacyOrderItem> items = request.getItems().stream()
                .map(itemRequest -> {
                    PharmacyOrderItem item = new PharmacyOrderItem();
                    item.setOrder(savedOrder);
                    item.setMedicineName(itemRequest.getMedicineName());
                    item.setQuantity(itemRequest.getQuantity());
                    item.setPrice(itemRequest.getPrice());
                    item.setTotalPrice(itemRequest.getPrice() * itemRequest.getQuantity());
                    item.setNotes(itemRequest.getNotes());
                    return item;
                })
                .collect(Collectors.toList());

        itemRepository.saveAll(items);

        return convertToResponse(savedOrder);
    }

    public PharmacyOrderResponse getPharmacyOrderById(Long id) {
        PharmacyOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Pharmacy order not found with id: " + id));

        User currentUser = authService.getCurrentUser();
        
        // Patient can only see their own orders, ADMIN and DOCTOR can see all
        if ("PATIENT".equals(currentUser.getRole()) && !order.getPatient().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You can only view your own pharmacy orders");
        }

        return convertToResponse(order);
    }

    public List<PharmacyOrderResponse> getMyPharmacyOrders() {
        User currentUser = authService.getCurrentUser();
        List<PharmacyOrder> orders = orderRepository.findByPatientId(currentUser.getId());
        return orders.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<PharmacyOrderResponse> getAllPharmacyOrders(String status) {
        User currentUser = authService.getCurrentUser();
        
        // Only ADMIN and DOCTOR can view all orders
        if (!"ADMIN".equals(currentUser.getRole()) && !"DOCTOR".equals(currentUser.getRole())) {
            throw new ForbiddenException("Only ADMIN and DOCTOR can view all pharmacy orders");
        }

        List<PharmacyOrder> orders;
        if (status != null && !status.isEmpty()) {
            orders = orderRepository.findByStatus(status);
        } else {
            orders = orderRepository.findAll();
        }

        return orders.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<PharmacyOrderResponse> getPharmacyOrdersByClinic(Long clinicId, String status) {
        User currentUser = authService.getCurrentUser();
        
        // Only ADMIN and DOCTOR can view orders by clinic
        if (!"ADMIN".equals(currentUser.getRole()) && !"DOCTOR".equals(currentUser.getRole())) {
            throw new ForbiddenException("Only ADMIN and DOCTOR can view pharmacy orders by clinic");
        }

        clinicRepository.findById(clinicId)
                .orElseThrow(() -> new NotFoundException("Clinic not found with id: " + clinicId));

        List<PharmacyOrder> orders;
        if (status != null && !status.isEmpty()) {
            orders = orderRepository.findByClinicIdAndStatus(clinicId, status);
        } else {
            orders = orderRepository.findByClinicId(clinicId);
        }

        return orders.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public PharmacyOrderResponse updatePharmacyOrderStatus(Long id, String status) {
        PharmacyOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Pharmacy order not found with id: " + id));

        User currentUser = authService.getCurrentUser();
        
        // Only ADMIN and DOCTOR can update status
        if (!"ADMIN".equals(currentUser.getRole()) && !"DOCTOR".equals(currentUser.getRole())) {
            throw new ForbiddenException("Only ADMIN and DOCTOR can update pharmacy order status");
        }

        // Validate status
        if (!"PENDING".equals(status) && !"PROCESSING".equals(status) && 
            !"READY".equals(status) && !"OUT_FOR_DELIVERY".equals(status) && 
            !"DELIVERED".equals(status) && !"CANCELLED".equals(status)) {
            throw new BadRequestException("Invalid status");
        }

        order.setStatus(status);

        if ("PROCESSING".equals(status)) {
            order.setProcessedAt(java.time.LocalDateTime.now());
        } else if ("DELIVERED".equals(status)) {
            order.setDeliveredAt(java.time.LocalDateTime.now());
        }

        PharmacyOrder updated = orderRepository.save(order);
        return convertToResponse(updated);
    }

    private PharmacyOrderResponse convertToResponse(PharmacyOrder order) {
        PharmacyOrderResponse response = new PharmacyOrderResponse();
        response.setId(order.getId());
        response.setPatientId(order.getPatient().getId());
        response.setPatientName(order.getPatient().getFullName());
        response.setAppointmentId(order.getAppointment() != null ? order.getAppointment().getId() : null);
        response.setClinicId(order.getClinic().getId());
        response.setClinicName(order.getClinic().getName());
        response.setPrescriptionFileUrl(order.getPrescriptionFileUrl());
        response.setStatus(order.getStatus());
        response.setDeliveryAddress(order.getDeliveryAddress());
        response.setDeliveryPhone(order.getDeliveryPhone());
        response.setDeliveryName(order.getDeliveryName());
        response.setPaymentMethod(order.getPaymentMethod());
        response.setTotalAmount(order.getTotalAmount());
        response.setDeliveryFee(order.getDeliveryFee());
        response.setNotes(order.getNotes());
        response.setCreatedAt(order.getCreatedAt());
        response.setProcessedAt(order.getProcessedAt());
        response.setDeliveredAt(order.getDeliveredAt());

        // Load order items
        List<PharmacyOrderItem> items = itemRepository.findByOrderId(order.getId());
        List<PharmacyOrderItemResponse> itemResponses = items.stream()
                .map(item -> {
                    PharmacyOrderItemResponse itemResponse = new PharmacyOrderItemResponse();
                    itemResponse.setId(item.getId());
                    itemResponse.setMedicineName(item.getMedicineName());
                    itemResponse.setQuantity(item.getQuantity());
                    itemResponse.setPrice(item.getPrice());
                    itemResponse.setTotalPrice(item.getTotalPrice());
                    itemResponse.setNotes(item.getNotes());
                    return itemResponse;
                })
                .collect(Collectors.toList());
        response.setItems(itemResponses);

        return response;
    }
}

