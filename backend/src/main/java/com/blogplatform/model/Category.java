package com.blogplatform.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "categories")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Category {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 80)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "color_hex", length = 7)
    @Builder.Default private String colorHex = "#6366f1";

    @Column(length = 60)
    private String icon;

    @Column(name = "created_at", updatable = false)
    @Builder.Default private LocalDateTime createdAt = LocalDateTime.now();
}
