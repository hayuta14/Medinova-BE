package com.project.medinova.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PharmacyOrderItemResponse {
    private Long id;
    private String medicineName;
    private Integer quantity;
    private Double price;
    private Double totalPrice;
    private String notes;
}

