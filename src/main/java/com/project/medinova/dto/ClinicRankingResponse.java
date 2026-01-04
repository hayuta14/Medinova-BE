package com.project.medinova.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClinicRankingResponse {
    private List<ClinicRankingItem> topClinics;
    private Long totalClinics;
}

