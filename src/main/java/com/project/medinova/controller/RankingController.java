package com.project.medinova.controller;

import com.project.medinova.dto.ClinicRankingResponse;
import com.project.medinova.dto.DoctorRankingResponse;
import com.project.medinova.service.RankingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ranking")
@CrossOrigin(origins = "*")
@Tag(name = "Ranking", description = "Ranking APIs for doctors and clinics")
public class RankingController {

    @Autowired
    private RankingService rankingService;

    @Operation(
            summary = "Get doctor ranking",
            description = "Get top doctors ranked by rating, reviews, and appointments. (ADMIN only)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Doctor ranking retrieved successfully",
                    content = @Content(schema = @Schema(implementation = DoctorRankingResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Only ADMIN can access")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/doctors")
    public ResponseEntity<DoctorRankingResponse> getDoctorRanking(
            @RequestParam(required = false, defaultValue = "10") int limit) {
        if (limit < 1) limit = 10;
        if (limit > 100) limit = 100;
        DoctorRankingResponse ranking = rankingService.getDoctorRanking(limit);
        return ResponseEntity.ok(ranking);
    }

    @Operation(
            summary = "Get clinic ranking",
            description = "Get top clinics ranked by appointments, doctors, and ratings. (ADMIN only)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Clinic ranking retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ClinicRankingResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Only ADMIN can access")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/clinics")
    public ResponseEntity<ClinicRankingResponse> getClinicRanking(
            @RequestParam(required = false, defaultValue = "10") int limit) {
        if (limit < 1) limit = 10;
        if (limit > 100) limit = 100;
        ClinicRankingResponse ranking = rankingService.getClinicRanking(limit);
        return ResponseEntity.ok(ranking);
    }
}



