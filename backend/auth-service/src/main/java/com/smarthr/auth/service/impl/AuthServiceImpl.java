package com.smarthr.auth.service.impl;

import java.io.File;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;

import com.smarthr.auth.dto.req.LoginReq;
import com.smarthr.auth.dto.req.RegisterReq;
import com.smarthr.auth.dto.res.AuthRes;
import com.smarthr.auth.model.entity.User;
import com.smarthr.auth.model.enums.Role;
import com.smarthr.auth.model.enums.UserStatus;
import com.smarthr.auth.repo.CompanyRepo;
import com.smarthr.auth.repo.UserRepo;
import com.smarthr.auth.service.AuthService;

import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepo userRepo;
    private final CompanyRepo companyRepo;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.private-key-path}")
    private String privateKeyPath;

    @Value("${jwt.expiration-ms}")
    private long jwtExpirationMs;

    @Override
    @Transactional
    public void register(RegisterReq req) {
        if (userRepo.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        Role role = Role.valueOf(req.getRole().toUpperCase());
        
        if (role == Role.HR) {
            if (req.getCompanyId() == null) {
                throw new RuntimeException("Company ID is required for HR role");
            }
            if (!companyRepo.existsById(req.getCompanyId())) {
                throw new RuntimeException("Company does not exist");
            }
        }

        UserStatus status = (role == Role.HR) ? UserStatus.PENDING : UserStatus.ACTIVE;

        User user = User.builder()
                .email(req.getEmail())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .role(role)
                .status(status)
                .companyId(role == Role.HR ? req.getCompanyId() : null)
                .build();

        userRepo.save(user);
    }

    @Override
    public AuthRes login(LoginReq req) {
        User user = userRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }

        if (user.getStatus() == UserStatus.LOCKED) {
            throw new RuntimeException("Account is locked");
        }
        
        if (user.getStatus() == UserStatus.PENDING) {
             throw new RuntimeException("Account is pending approval");
        }

        String accessToken = generateAccessToken(user);
        String refreshToken = UUID.randomUUID().toString(); 
        
        //Refresh token storage in Redis is deferred to complete the MVP login flow first

        return AuthRes.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    private String generateAccessToken(User user) {
        try {
            File file = ResourceUtils.getFile(privateKeyPath);
            String key = new String(Files.readAllBytes(file.toPath()));
            key = key.replace("-----BEGIN PRIVATE KEY-----", "")
                     .replace("-----END PRIVATE KEY-----", "")
                     .replaceAll("\\s+", "");

            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(key));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

            Instant now = Instant.now();
            return Jwts.builder()
                    .subject(user.getId().toString())
                    .claim("role", user.getRole().name())
                    .claim("companyId", user.getCompanyId() != null ? user.getCompanyId().toString() : null)
                    .id(UUID.randomUUID().toString())
                    .issuedAt(Date.from(now))
                    .expiration(Date.from(now.plus(jwtExpirationMs, ChronoUnit.MILLIS)))
                    .signWith(privateKey)
                    .compact();

        } catch (Exception e) {
            throw new RuntimeException("Error generating token", e);
        }
    }
}