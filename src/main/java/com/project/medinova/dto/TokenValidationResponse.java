package com.project.medinova.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenValidationResponse {
    private boolean valid;
    private boolean expired;
    private LocalDateTime expirationDate;
    private String message;
    private Long userId;
    private String email;
    private String role;
}

