// ─── Role.java ───────────────────────────────────────────────────────
package com.blogplatform.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "roles")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Role {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 30)
    private String name; // ROLE_USER | ROLE_ADMIN
}
