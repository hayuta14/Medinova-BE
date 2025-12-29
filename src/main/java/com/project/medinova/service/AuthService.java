package com.project.medinova.service;

import com.project.medinova.config.JwtTokenProvider;
import com.project.medinova.dto.AuthRequest;
import com.project.medinova.dto.AuthResponse;
import com.project.medinova.dto.RegisterRequest;
import com.project.medinova.dto.TokenValidationResponse;
import com.project.medinova.entity.User;
import com.project.medinova.exception.BadRequestException;
import com.project.medinova.exception.UnauthorizedException;
import com.project.medinova.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Service
@Transactional
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private AuthenticationManager authenticationManager;

    private final Set<String> blacklistedTokens = new HashSet<>();

    public AuthResponse login(AuthRequest authRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getEmail(),
                            authRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            User user = userRepository.findByEmail(authRequest.getEmail())
                    .orElseThrow(() -> new UnauthorizedException("User not found"));

            if (user.getStatus() != null && !user.getStatus().equals("ACTIVE")) {
                throw new UnauthorizedException("Account is not active");
            }

            String token = tokenProvider.generateToken(user.getEmail(), user.getId(), user.getRole());

            AuthResponse authResponse = new AuthResponse();
            authResponse.setToken(token);
            authResponse.setTokenType("Bearer");
            authResponse.setUserId(user.getId());
            authResponse.setEmail(user.getEmail());
            authResponse.setFullName(user.getFullName());
            authResponse.setRole(user.getRole());

            return authResponse;
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            throw new UnauthorizedException("Invalid email or password");
        }
    }

    public void logout(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (token != null && !token.isEmpty()) {
            blacklistedTokens.add(token);
        }
    }

    public boolean isTokenBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("User not authenticated");
        }

        // Lấy userId từ authentication details (được set bởi JwtAuthenticationFilter)
        Long userId = null;
        if (authentication.getDetails() instanceof java.util.Map) {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> details = (java.util.Map<String, Object>) authentication.getDetails();
            Object userIdObj = details.get("userId");
            if (userIdObj instanceof Number) {
                userId = ((Number) userIdObj).longValue();
            }
        }

        // Nếu có userId từ token, query trực tiếp bằng userId
        if (userId != null) {
            return userRepository.findById(userId)
                    .orElseThrow(() -> new UnauthorizedException("User not found"));
        }

        // Fallback: lấy từ email nếu không có userId (trường hợp cũ)
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
    }

    @Transactional
    public AuthResponse register(RegisterRequest registerRequest) {
        // Kiểm tra email đã tồn tại chưa
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        // Tạo user mới với role PATIENT
        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));
        user.setFullName(registerRequest.getFullName());
        user.setPhone(registerRequest.getPhone());
        user.setRole("PATIENT");
        user.setStatus("ACTIVE");

        user = userRepository.save(user);

        // Tạo JWT token và trả về response
        String token = tokenProvider.generateToken(user.getEmail(), user.getId(), user.getRole());

        AuthResponse authResponse = new AuthResponse();
        authResponse.setToken(token);
        authResponse.setTokenType("Bearer");
        authResponse.setUserId(user.getId());
        authResponse.setEmail(user.getEmail());
        authResponse.setFullName(user.getFullName());
        authResponse.setRole(user.getRole());

        return authResponse;
    }

    public TokenValidationResponse validateToken(String token) {
        TokenValidationResponse response = new TokenValidationResponse();
        
        if (token == null || token.isEmpty()) {
            response.setValid(false);
            response.setExpired(true);
            response.setMessage("Token is empty or null");
            return response;
        }

        // Remove "Bearer " prefix if present
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // Check if token is blacklisted
        if (isTokenBlacklisted(token)) {
            response.setValid(false);
            response.setExpired(true);
            response.setMessage("Token has been revoked (logged out)");
            return response;
        }

        try {
            // Validate token
            boolean isValid = tokenProvider.validateToken(token);
            Date expirationDate = tokenProvider.getExpirationDateFromToken(token);
            boolean isExpired = expirationDate.before(new Date());

            response.setValid(isValid && !isExpired);
            response.setExpired(isExpired);
            
            if (expirationDate != null) {
                LocalDateTime expirationDateTime = LocalDateTime.ofInstant(
                    expirationDate.toInstant(), ZoneId.systemDefault());
                response.setExpirationDate(expirationDateTime);
            }

            if (isValid && !isExpired) {
                // Get user info from token
                String email = tokenProvider.getUsernameFromToken(token);
                Long userId = tokenProvider.getUserIdFromToken(token);
                String role = tokenProvider.getRoleFromToken(token);

                response.setUserId(userId);
                response.setEmail(email);
                response.setRole(role);
                response.setMessage("Token is valid");
            } else {
                response.setMessage("Token is expired");
            }
        } catch (Exception e) {
            response.setValid(false);
            response.setExpired(true);
            response.setMessage("Invalid token: " + e.getMessage());
        }

        return response;
    }
}

