package com.project.medinova.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorRankingResponse {
    private List<DoctorRankingItem> topDoctors;
    private Long totalDoctors;
}




