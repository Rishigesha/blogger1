package com.blogplatform.controller;

import com.blogplatform.model.Category;
import com.blogplatform.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepo;

    @GetMapping
    public ResponseEntity<?> all() { return ResponseEntity.ok(categoryRepo.findAll()); }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> create(@RequestBody Category cat) {
        if (categoryRepo.existsByName(cat.getName()))
            return ResponseEntity.badRequest().body(Map.of("message", "Category already exists."));
        cat.setSlug(cat.getName().toLowerCase().replaceAll("[^a-z0-9]+", "-"));
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryRepo.save(cat));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody Category req) {
        return categoryRepo.findById(id).map(c -> {
            c.setName(req.getName());
            c.setDescription(req.getDescription());
            c.setColorHex(req.getColorHex());
            c.setIcon(req.getIcon());
            return ResponseEntity.ok(categoryRepo.save(c));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        categoryRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Category deleted."));
    }
}
