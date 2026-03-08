package com.blogplatform.controller;

import com.blogplatform.model.BlogPost.PostStatus;
import com.blogplatform.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepo;
    private final BlogPostRepository postRepo;
    private final CommentRepository commentRepo;

    // ── Dashboard stats ───────────────────────────────────────────────
    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        return ResponseEntity.ok(Map.of(
            "totalUsers",       userRepo.count(),
            "activeUsers",      userRepo.countByIsActive(true),
            "totalPosts",       postRepo.count(),
            "publishedPosts",   postRepo.countByStatus(PostStatus.PUBLISHED),
            "draftPosts",       postRepo.countByStatus(PostStatus.DRAFT),
            "totalComments",    commentRepo.count(),
            "pendingComments",  commentRepo.countByIsApproved(false)
        ));
    }

    // ── All users ─────────────────────────────────────────────────────
    @GetMapping("/users")
    public ResponseEntity<?> getUsers(@RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "10") int size) {
        Pageable p = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(userRepo.findAll(p));
    }

    // ── Block / unblock user ──────────────────────────────────────────
    @PatchMapping("/users/{id}/toggle-active")
    public ResponseEntity<?> toggleUser(@PathVariable Long id) {
        return userRepo.findById(id).map(u -> {
            u.setIsActive(!u.getIsActive());
            userRepo.save(u);
            return ResponseEntity.ok(Map.of(
                "message", "User " + (u.getIsActive() ? "unblocked" : "blocked"),
                "isActive", u.getIsActive()
            ));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ── All comments (pending) ────────────────────────────────────────
    @GetMapping("/comments")
    public ResponseEntity<?> getComments(@RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "10") int size,
                                         @RequestParam(defaultValue = "false") boolean pendingOnly) {
        Pageable p = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(pendingOnly
            ? commentRepo.findByIsApproved(false, p)
            : commentRepo.findAll(p));
    }
}
