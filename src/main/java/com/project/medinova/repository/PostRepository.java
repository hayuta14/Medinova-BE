package com.project.medinova.repository;

import com.project.medinova.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByAuthorId(Long authorId);
    List<Post> findByStatus(String status);
    List<Post> findByAuthorIdAndStatus(Long authorId, String status);
}

