package com.blogplatform.repository;

import com.blogplatform.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Integer> {
    Optional<Tag> findBySlug(String slug);
    Optional<Tag> findByName(String name);
}
