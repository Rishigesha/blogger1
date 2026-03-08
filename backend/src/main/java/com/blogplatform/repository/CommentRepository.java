package com.blogplatform.repository;

import com.blogplatform.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostIdAndParentIsNullAndIsApprovedTrueOrderByCreatedAtDesc(Long postId);
    Page<Comment> findByIsApproved(Boolean approved, Pageable pageable);
    long countByIsApproved(Boolean approved);
}
