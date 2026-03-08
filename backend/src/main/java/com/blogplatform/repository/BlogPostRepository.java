package com.blogplatform.repository;

import com.blogplatform.model.BlogPost;
import com.blogplatform.model.BlogPost.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {
    Optional<BlogPost> findBySlugAndStatus(String slug, PostStatus status);
    Page<BlogPost> findByStatus(PostStatus status, Pageable pageable);
    Page<BlogPost> findByCategoryIdAndStatus(Integer categoryId, PostStatus status, Pageable pageable);
    List<BlogPost> findTop5ByStatusOrderByViewCountDesc(PostStatus status);
    List<BlogPost> findByIsFeaturedTrueAndStatus(PostStatus status);
    Page<BlogPost> findByAuthorId(Long authorId, Pageable pageable);
    long countByStatus(PostStatus status);

    @Query("SELECT p FROM BlogPost p WHERE p.status = 'PUBLISHED' AND " +
           "(LOWER(p.title) LIKE LOWER(CONCAT('%',:q,'%')) OR LOWER(p.content) LIKE LOWER(CONCAT('%',:q,'%')))")
    Page<BlogPost> search(String q, Pageable pageable);

    @Modifying
    @Query("UPDATE BlogPost p SET p.viewCount = p.viewCount + 1 WHERE p.id = :id")
    void incrementViewCount(Long id);
}
