package com.blogplatform.controller;

import com.blogplatform.dto.request.PostRequest;
import com.blogplatform.model.BlogPost;
import com.blogplatform.model.BlogPost.PostStatus;
import com.blogplatform.model.Category;
import com.blogplatform.model.Tag;
import com.blogplatform.model.User;
import com.blogplatform.repository.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BlogPostController {

    private final BlogPostRepository postRepo;
    private final UserRepository userRepo;
    private final CategoryRepository categoryRepo;
    private final TagRepository tagRepo;

    // ── Public: list published posts ──────────────────────────────────
    @GetMapping("/posts")
    public ResponseEntity<?> getPosts(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) Integer categoryId,
        @RequestParam(required = false) String search
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
        Page<BlogPost> result;

        if (search != null && !search.isBlank()) {
            result = postRepo.search(search, pageable);
        } else if (categoryId != null) {
            result = postRepo.findByCategoryIdAndStatus(categoryId, PostStatus.PUBLISHED, pageable);
        } else {
            result = postRepo.findByStatus(PostStatus.PUBLISHED, pageable);
        }
        return ResponseEntity.ok(result);
    }

    // ── Public: get single post by slug ───────────────────────────────
    @GetMapping("/posts/{slug}")
    public ResponseEntity<?> getPost(@PathVariable String slug) {
        return postRepo.findBySlugAndStatus(slug, PostStatus.PUBLISHED)
            .map(post -> {
                postRepo.incrementViewCount(post.getId());
                return ResponseEntity.ok(post);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    // ── Public: featured posts ────────────────────────────────────────
    @GetMapping("/posts/featured")
    public ResponseEntity<?> getFeatured() {
        return ResponseEntity.ok(postRepo.findByIsFeaturedTrueAndStatus(PostStatus.PUBLISHED));
    }

    // ── Public: trending posts ────────────────────────────────────────
    @GetMapping("/posts/trending")
    public ResponseEntity<?> getTrending() {
        return ResponseEntity.ok(postRepo.findTop5ByStatusOrderByViewCountDesc(PostStatus.PUBLISHED));
    }

    // ── Admin: create post ────────────────────────────────────────────
    @PostMapping("/admin/posts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createPost(@Valid @RequestBody PostRequest req,
                                        @AuthenticationPrincipal UserDetails ud) {
        User author = userRepo.findByUsername(ud.getUsername()).orElseThrow();
        BlogPost post = buildPost(req, author);
        return ResponseEntity.status(HttpStatus.CREATED).body(postRepo.save(post));
    }

    // ── Admin: update post ────────────────────────────────────────────
    @PutMapping("/admin/posts/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updatePost(@PathVariable Long id,
                                        @Valid @RequestBody PostRequest req) {
        return postRepo.findById(id).map(post -> {
            applyRequest(post, req);
            return ResponseEntity.ok(postRepo.save(post));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ── Admin: delete post ────────────────────────────────────────────
    @DeleteMapping("/admin/posts/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deletePost(@PathVariable Long id) {
        postRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Post deleted."));
    }

    // ── Admin: all posts (including drafts) ───────────────────────────
    @GetMapping("/admin/posts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> adminGetPosts(@RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "10") int size) {
        Pageable p = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(postRepo.findAll(p));
    }

    // ── Helpers ───────────────────────────────────────────────────────
    private BlogPost buildPost(PostRequest req, User author) {
        BlogPost post = new BlogPost();
        post.setAuthor(author);
        applyRequest(post, req);
        return post;
    }

    private void applyRequest(BlogPost post, PostRequest req) {
        post.setTitle(req.getTitle());
        post.setSlug(slugify(req.getTitle()) + "-" + System.currentTimeMillis());
        post.setExcerpt(req.getExcerpt());
        post.setContent(req.getContent());
        post.setCoverImage(req.getCoverImage());
        post.setSeoTitle(req.getSeoTitle());
        post.setSeoKeywords(req.getSeoKeywords());
        if (req.getIsFeatured() != null) post.setIsFeatured(req.getIsFeatured());
        if (req.getCategoryId() != null)
            categoryRepo.findById(req.getCategoryId()).ifPresent(post::setCategory);
        if (req.getStatus() != null) {
            PostStatus s = PostStatus.valueOf(req.getStatus().toUpperCase());
            post.setStatus(s);
            if (s == PostStatus.PUBLISHED && post.getPublishedAt() == null)
                post.setPublishedAt(LocalDateTime.now());
        }
        if (req.getTags() != null) {
            Set<Tag> tags = new HashSet<>();
            for (String t : req.getTags()) {
                String slug = slugify(t);
                tags.add(tagRepo.findBySlug(slug).orElseGet(() -> {
                    Tag newTag = new Tag(); newTag.setName(t); newTag.setSlug(slug);
                    return tagRepo.save(newTag);
                }));
            }
            post.setTags(tags);
        }
        int words = req.getContent().split("\\s+").length;
        post.setReadTimeMin(Math.max(1, words / 200));
    }

    private String slugify(String input) {
        return input.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
    }
}
