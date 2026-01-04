package com.project.medinova.service;

import com.project.medinova.dto.CreatePostRequest;
import com.project.medinova.dto.PostResponse;
import com.project.medinova.dto.UpdatePostRequest;
import com.project.medinova.entity.Post;
import com.project.medinova.entity.User;
import com.project.medinova.exception.BadRequestException;
import com.project.medinova.exception.ForbiddenException;
import com.project.medinova.exception.NotFoundException;
import com.project.medinova.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private AuthService authService;

    public PostResponse createPost(CreatePostRequest request) {
        User currentUser = authService.getCurrentUser();
        
        // Only ADMIN can create posts
        if (!"ADMIN".equals(currentUser.getRole())) {
            throw new ForbiddenException("Only ADMIN can create posts");
        }

        Post post = new Post();
        post.setAuthor(currentUser);
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setStatus(request.getStatus() != null ? request.getStatus() : "DRAFT");

        if (!"DRAFT".equals(post.getStatus()) && !"PUBLISHED".equals(post.getStatus())) {
            throw new BadRequestException("Status must be DRAFT or PUBLISHED");
        }

        Post savedPost = postRepository.save(post);
        return convertToResponse(savedPost);
    }

    public PostResponse getPostById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Post not found with id: " + id));
        return convertToResponse(post);
    }

    public List<PostResponse> getAllPosts(String status) {
        List<Post> posts;
        if (status != null && !status.isEmpty()) {
            posts = postRepository.findByStatus(status);
        } else {
            posts = postRepository.findAll();
        }
        return posts.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public Page<PostResponse> getAllPosts(Pageable pageable, String status) {
        Page<Post> posts;
        if (status != null && !status.isEmpty()) {
            List<Post> allPosts = postRepository.findByStatus(status);
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), allPosts.size());
            List<Post> pageContent = allPosts.subList(start, end);
            posts = new org.springframework.data.domain.PageImpl<>(pageContent, pageable, allPosts.size());
        } else {
            posts = postRepository.findAll(pageable);
        }
        return posts.map(this::convertToResponse);
    }

    public List<PostResponse> getPublishedPosts() {
        List<Post> posts = postRepository.findByStatus("PUBLISHED");
        return posts.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<PostResponse> getMyPosts() {
        User currentUser = authService.getCurrentUser();
        List<Post> posts = postRepository.findByAuthorId(currentUser.getId());
        return posts.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public PostResponse updatePost(Long id, UpdatePostRequest request) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Post not found with id: " + id));

        User currentUser = authService.getCurrentUser();
        
        // Only author or ADMIN can update
        if (!post.getAuthor().getId().equals(currentUser.getId()) && !"ADMIN".equals(currentUser.getRole())) {
            throw new ForbiddenException("You can only update your own posts");
        }

        if (request.getTitle() != null) {
            post.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            post.setContent(request.getContent());
        }
        if (request.getStatus() != null) {
            if (!"DRAFT".equals(request.getStatus()) && !"PUBLISHED".equals(request.getStatus())) {
                throw new BadRequestException("Status must be DRAFT or PUBLISHED");
            }
            post.setStatus(request.getStatus());
        }

        Post updatedPost = postRepository.save(post);
        return convertToResponse(updatedPost);
    }

    public void deletePost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Post not found with id: " + id));

        User currentUser = authService.getCurrentUser();
        
        // Only author or ADMIN can delete
        if (!post.getAuthor().getId().equals(currentUser.getId()) && !"ADMIN".equals(currentUser.getRole())) {
            throw new ForbiddenException("You can only delete your own posts");
        }

        postRepository.delete(post);
    }

    private PostResponse convertToResponse(Post post) {
        PostResponse response = new PostResponse();
        response.setId(post.getId());
        response.setAuthorId(post.getAuthor().getId());
        response.setAuthorName(post.getAuthor().getFullName());
        response.setTitle(post.getTitle());
        response.setContent(post.getContent());
        response.setStatus(post.getStatus());
        response.setCreatedAt(post.getCreatedAt());
        return response;
    }
}

