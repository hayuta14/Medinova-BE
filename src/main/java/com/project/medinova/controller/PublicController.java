package com.project.medinova.controller;

import com.project.medinova.dto.PublicStatsResponse;
import com.project.medinova.service.PublicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public")
@CrossOrigin(origins = "*")
@Tag(name = "Public", description = "Public APIs for homepage")
public class PublicController {

    @Autowired
    private PublicService publicService;

    @Operation(
            summary = "Get public stats",
            description = "Get public statistics for homepage including featured doctors, clinics, and recent posts. This endpoint is public and does not require authentication."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Public stats retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PublicStatsResponse.class))
            )
    })
    @GetMapping("/stats")
    public ResponseEntity<PublicStatsResponse> getPublicStats() {
        PublicStatsResponse stats = publicService.getPublicStats();
        return ResponseEntity.ok(stats);
    }
}




