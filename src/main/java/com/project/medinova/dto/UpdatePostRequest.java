package com.project.medinova.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdatePostRequest {
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    private String content;

    private String status; // DRAFT | PUBLISHED
}

