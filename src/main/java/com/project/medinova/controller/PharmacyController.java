package com.project.medinova.controller;

import com.project.medinova.dto.CreatePharmacyOrderRequest;
import com.project.medinova.dto.PharmacyOrderResponse;
import com.project.medinova.service.PharmacyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pharmacy-orders")
@CrossOrigin(origins = "*")
@Tag(name = "Pharmacy Order Management", description = "Pharmacy order management APIs")
public class PharmacyController {

    @Autowired
    private PharmacyService pharmacyService;

    @Operation(
            summary = "Create pharmacy order",
            description = "Create a new pharmacy order. Only patients can create orders. Can link to appointment or upload prescription."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Pharmacy order created successfully",
                    content = @Content(schema = @Schema(implementation = PharmacyOrderResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad request - Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Only patients can create orders"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Clinic or appointment not found")
    })
    @PreAuthorize("hasRole('PATIENT')")
    @PostMapping
    public ResponseEntity<PharmacyOrderResponse> createPharmacyOrder(@Valid @RequestBody CreatePharmacyOrderRequest request) {
        PharmacyOrderResponse response = pharmacyService.createPharmacyOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Get pharmacy order by ID",
            description = "Get pharmacy order information by ID. Patients can only view their own orders, ADMIN and DOCTOR can view all."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Pharmacy order retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PharmacyOrderResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Cannot view other patients' orders"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Pharmacy order not found")
    })
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<PharmacyOrderResponse> getPharmacyOrderById(@PathVariable Long id) {
        PharmacyOrderResponse response = pharmacyService.getPharmacyOrderById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get my pharmacy orders",
            description = "Get all pharmacy orders created by the current patient."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Pharmacy orders retrieved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/my-orders")
    public ResponseEntity<List<PharmacyOrderResponse>> getMyPharmacyOrders() {
        List<PharmacyOrderResponse> orders = pharmacyService.getMyPharmacyOrders();
        return ResponseEntity.ok(orders);
    }

    @Operation(
            summary = "Get all pharmacy orders",
            description = "Get all pharmacy orders with optional status filter. Only ADMIN and DOCTOR can access."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Pharmacy orders retrieved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Only ADMIN and DOCTOR can access")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @GetMapping
    public ResponseEntity<List<PharmacyOrderResponse>> getAllPharmacyOrders(
            @RequestParam(required = false) String status) {
        List<PharmacyOrderResponse> orders = pharmacyService.getAllPharmacyOrders(status);
        return ResponseEntity.ok(orders);
    }

    @Operation(
            summary = "Get pharmacy orders by clinic",
            description = "Get all pharmacy orders for a specific clinic with optional status filter. Only ADMIN and DOCTOR can access."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Pharmacy orders retrieved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Only ADMIN and DOCTOR can access"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Clinic not found")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @GetMapping("/clinics/{clinicId}")
    public ResponseEntity<List<PharmacyOrderResponse>> getPharmacyOrdersByClinic(
            @PathVariable Long clinicId,
            @RequestParam(required = false) String status) {
        List<PharmacyOrderResponse> orders = pharmacyService.getPharmacyOrdersByClinic(clinicId, status);
        return ResponseEntity.ok(orders);
    }

    @Operation(
            summary = "Update pharmacy order status",
            description = "Update pharmacy order status (PENDING, PROCESSING, READY, OUT_FOR_DELIVERY, DELIVERED, CANCELLED). Only ADMIN and DOCTOR can update."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Pharmacy order status updated successfully",
                    content = @Content(schema = @Schema(implementation = PharmacyOrderResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad request - Invalid status"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Only ADMIN and DOCTOR can update"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Pharmacy order not found")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @PutMapping("/{id}/status")
    public ResponseEntity<PharmacyOrderResponse> updatePharmacyOrderStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        PharmacyOrderResponse response = pharmacyService.updatePharmacyOrderStatus(id, status);
        return ResponseEntity.ok(response);
    }
}






