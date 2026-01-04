package com.project.medinova.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {
    private Long id;
    private Long authorId;
    private String authorName;
    private String title;
    private String content;
    private String status;
    private LocalDateTime createdAt;
}

