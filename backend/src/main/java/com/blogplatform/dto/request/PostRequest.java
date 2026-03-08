package com.blogplatform.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.Set;

@Data
public class PostRequest {
    @NotBlank @Size(max = 255) private String title;
    @Size(max = 500) private String excerpt;
    @NotBlank private String content;
    private String coverImage;
    private Integer categoryId;
    private Set<String> tags;
    private String status; // DRAFT | PUBLISHED
    private Boolean isFeatured;
    private String seoTitle;
    private String seoKeywords;
}
