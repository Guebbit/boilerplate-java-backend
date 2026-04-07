package com.guebbit.backend.service;

import com.guebbit.backend.common.ApiException;
import com.guebbit.backend.model.UserDocument;
import com.guebbit.backend.repository.UserRepository;
import com.guebbit.backend.security.AppPrincipal;
import com.guebbit.backend.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public Map<String, Object> signup(String email, String username, String password, String passwordConfirm, String imageUrl) {
        if (!password.equals(passwordConfirm)) throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "PASSWORD_CONFIRM_MISMATCH");
        userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(email).ifPresent(u -> {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "EMAIL_ALREADY_EXISTS");
        });
        UserDocument user = new UserDocument();
        user.email = email.toLowerCase();
        user.username = username;
        user.passwordHash = passwordEncoder.encode(password);
        user.admin = false;
        user.active = true;
        user.imageUrl = imageUrl;
        user.createdAt = Instant.now();
        user.updatedAt = Instant.now();
        userRepository.save(user);
        return userPayload(user);
    }

    public Map<String, Object> login(String email, String password) {
        UserDocument user = userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(email)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS"));
        if (!user.active || !passwordEncoder.matches(password, user.passwordHash)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS");
        }
        String token = jwtService.createAccessToken(user.id, user.email, user.admin);
        String refreshToken = jwtService.createRefreshToken(user.id);
        UserDocument.TokenEntry entry = new UserDocument.TokenEntry();
        entry.type = "refresh";
        entry.token = refreshToken;
        entry.expiration = Instant.now().plusSeconds(60L * 60 * 24 * 7);
        user.tokens.add(entry);
        user.updatedAt = Instant.now();
        userRepository.save(user);
        return Map.of("token", token, "refreshToken", refreshToken, "expiresIn", 3600);
    }

    public Map<String, Object> refresh(String refreshToken) {
        String userId;
        try {
            userId = jwtService.parseRefresh(refreshToken).getSubject();
        } catch (Exception ex) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN");
        }
        UserDocument user = userRepository.findById(userId).orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN"));
        boolean exists = user.tokens.stream().anyMatch(t -> "refresh".equals(t.type) && refreshToken.equals(t.token) && t.expiration.isAfter(Instant.now()));
        if (!exists) throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN");
        String accessToken = jwtService.createAccessToken(user.id, user.email, user.admin);
        return Map.of("token", accessToken, "expiresIn", 3600);
    }

    public void requestPasswordReset(String email) {
        Optional<UserDocument> existing = userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(email);
        if (existing.isEmpty()) return;
        UserDocument user = existing.get();
        UserDocument.TokenEntry entry = new UserDocument.TokenEntry();
        entry.type = "password-reset";
        entry.token = UUID.randomUUID().toString();
        entry.expiration = Instant.now().plusSeconds(60L * 30);
        user.tokens.add(entry);
        user.updatedAt = Instant.now();
        userRepository.save(user);
    }

    public void confirmPasswordReset(String token, String password, String confirm) {
        if (!password.equals(confirm)) throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "PASSWORD_CONFIRM_MISMATCH");
        List<UserDocument> users = userRepository.findAll();
        for (UserDocument user : users) {
            UserDocument.TokenEntry reset = user.tokens.stream()
                    .filter(t -> "password-reset".equals(t.type) && token.equals(t.token))
                    .findFirst().orElse(null);
            if (reset != null && reset.expiration.isAfter(Instant.now())) {
                user.passwordHash = passwordEncoder.encode(password);
                user.tokens.removeIf(t -> "password-reset".equals(t.type) && token.equals(t.token));
                user.updatedAt = Instant.now();
                userRepository.save(user);
                return;
            }
        }
        throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "INVALID_RESET_TOKEN");
    }

    public void logoutAll(AppPrincipal principal) {
        UserDocument user = userRepository.findById(principal.id()).orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "AUTH_REQUIRED"));
        user.tokens.removeIf(t -> "refresh".equals(t.type));
        user.updatedAt = Instant.now();
        userRepository.save(user);
    }

    public long deleteExpiredTokens() {
        long updated = 0;
        Instant now = Instant.now();
        for (UserDocument user : userRepository.findAll()) {
            int before = user.tokens.size();
            user.tokens.removeIf(t -> t.expiration != null && t.expiration.isBefore(now));
            if (user.tokens.size() != before) {
                user.updatedAt = now;
                userRepository.save(user);
                updated++;
            }
        }
        return updated;
    }

    public Map<String, Object> account(AppPrincipal principal) {
        UserDocument user = userRepository.findById(principal.id()).orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "AUTH_REQUIRED"));
        return userPayload(user);
    }

    private Map<String, Object> userPayload(UserDocument user) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", user.id);
        map.put("email", user.email);
        map.put("username", user.username);
        map.put("admin", user.admin);
        map.put("active", user.active);
        map.put("imageUrl", user.imageUrl);
        map.put("createdAt", user.createdAt);
        map.put("updatedAt", user.updatedAt);
        return map;
    }
}
