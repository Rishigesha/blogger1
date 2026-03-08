package com.blogplatform.controller;

import com.blogplatform.dto.request.LoginRequest;
import com.blogplatform.dto.request.RegisterRequest;
import com.blogplatform.model.Role;
import com.blogplatform.model.User;
import com.blogplatform.repository.RoleRepository;
import com.blogplatform.repository.UserRepository;
import com.blogplatform.security.jwt.JwtUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authManager;
    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;

    // ── User Register ─────────────────────────────────────────────────
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest req) {
        if (userRepo.existsByUsername(req.getUsername()))
            return badRequest("Username is already taken.");
        if (userRepo.existsByEmail(req.getEmail()))
            return badRequest("Email is already in use.");

        Role userRole = roleRepo.findByName("ROLE_USER")
            .orElseThrow(() -> new RuntimeException("ROLE_USER not found"));

        User user = User.builder()
            .username(req.getUsername())
            .email(req.getEmail())
            .password(encoder.encode(req.getPassword()))
            .fullName(req.getFullName())
            .role(userRole)
            .build();
        userRepo.save(user);
        return ResponseEntity.ok(Map.of("message", "User registered successfully."));
    }

    // ── Admin Register (protected - only existing admin can call) ──────
    @PostMapping("/admin/register")
    public ResponseEntity<?> registerAdmin(@Valid @RequestBody RegisterRequest req) {
        if (userRepo.existsByUsername(req.getUsername()))
            return badRequest("Username is already taken.");
        if (userRepo.existsByEmail(req.getEmail()))
            return badRequest("Email is already in use.");

        Role adminRole = roleRepo.findByName("ROLE_ADMIN")
            .orElseThrow(() -> new RuntimeException("ROLE_ADMIN not found"));

        User admin = User.builder()
            .username(req.getUsername())
            .email(req.getEmail())
            .password(encoder.encode(req.getPassword()))
            .fullName(req.getFullName())
            .role(adminRole)
            .isEmailVerified(true)
            .build();
        userRepo.save(admin);
        return ResponseEntity.ok(Map.of("message", "Admin registered successfully."));
    }

    // ── Login (shared for user & admin — role is returned in response) ─
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        try {
            Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsernameOrEmail(), req.getPassword())
            );
            String token = jwtUtils.generateToken(auth);
            String role = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).findFirst().orElse("ROLE_USER");

            User user = userRepo.findByUsernameOrEmail(req.getUsernameOrEmail(), req.getUsernameOrEmail())
                .orElseThrow();
            // update last login
            user.setLastLogin(java.time.LocalDateTime.now());
            userRepo.save(user);

            return ResponseEntity.ok(Map.of(
                "token", token,
                "type", "Bearer",
                "role", role,
                "userId", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "fullName", user.getFullName() != null ? user.getFullName() : "",
                "avatarUrl", user.getAvatarUrl() != null ? user.getAvatarUrl() : ""
            ));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Invalid username or password."));
        } catch (LockedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("message", "Account is blocked. Contact support."));
        }
    }

    private ResponseEntity<Map<String, String>> badRequest(String msg) {
        return ResponseEntity.badRequest().body(Map.of("message", msg));
    }
}
