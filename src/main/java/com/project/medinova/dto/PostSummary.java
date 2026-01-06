package com.project.medinova.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostSummary {
    private Long id;
    private String title;
    private String contentPreview;
    private String authorName;
    private LocalDateTime createdAt;
}




