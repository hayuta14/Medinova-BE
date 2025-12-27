package com.project.medinova.repository;

import com.project.medinova.entity.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostCommentRepository extends JpaRepository<PostComment, Long> {
    List<PostComment> findByPostId(Long postId);
    List<PostComment> findByUserId(Long userId);
    List<PostComment> findByParentCommentId(Long parentCommentId);
    List<PostComment> findByPostIdAndParentCommentIdIsNull(Long postId);
}

