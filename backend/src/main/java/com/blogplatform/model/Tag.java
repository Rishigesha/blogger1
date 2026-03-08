package com.blogplatform.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "tags")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Tag {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 60)
    private String name;

    @Column(nullable = false, unique = true, length = 80)
    private String slug;
}
