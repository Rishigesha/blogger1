package com.blogplatform.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

// ─── User Registration ────────────────────────────────────────────────
@Data
public class RegisterRequest {
    @NotBlank @Size(min = 3, max = 50)
    private String username;

    @NotBlank @Email
    private String email;

    @NotBlank @Size(min = 6, max = 100)
    private String password;

    @Size(max = 100)
    private String fullName;
}
