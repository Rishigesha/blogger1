package com.blogplatform.controller;

import com.blogplatform.model.*;
import com.blogplatform.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentController {

    private final CommentRepository commentRepo;
    private final BlogPostRepository postRepo;
    private final UserRepository userRepo;

    // ── Get comments for a post ───────────────────────────────────────
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<?> getComments(@PathVariable Long postId) {
        return ResponseEntity.ok(
            commentRepo.findByPostIdAndParentIsNullAndIsApprovedTrueOrderByCreatedAtDesc(postId)
        );
    }

    // ── Add comment (authenticated users) ────────────────────────────
    @PostMapping("/posts/{postId}/comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> addComment(@PathVariable Long postId,
                                        @RequestBody Map<String, Object> body,
                                        @AuthenticationPrincipal UserDetails ud) {
        BlogPost post = postRepo.findById(postId).orElseThrow();
        User user = userRepo.findByUsername(ud.getUsername()).orElseThrow();

        Comment comment = new Comment();
        comment.setPost(post);
        comment.setUser(user);
        comment.setBody((String) body.get("body"));

        Object parentId = body.get("parentId");
        if (parentId != null) {
            commentRepo.findById(Long.parseLong(parentId.toString()))
                .ifPresent(comment::setParent);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(commentRepo.save(comment));
    }

    // ── Delete comment ────────────────────────────────────────────────
    @DeleteMapping("/comments/{id}")
    @PreAuthorize("hasRole('ADMIN') or @commentSecurity.isOwner(#id, authentication)")
    public ResponseEntity<?> deleteComment(@PathVariable Long id) {
        commentRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Comment deleted."));
    }

    // ── Admin: moderate comment ───────────────────────────────────────
    @PatchMapping("/admin/comments/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approveComment(@PathVariable Long id) {
        return commentRepo.findById(id).map(c -> {
            c.setIsApproved(true);
            return ResponseEntity.ok(commentRepo.save(c));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/admin/comments/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> adminDeleteComment(@PathVariable Long id) {
        commentRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Comment deleted by admin."));
    }
}
