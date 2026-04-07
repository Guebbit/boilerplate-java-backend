package com.guebbit.backend.service;

import com.guebbit.backend.common.ApiException;
import com.guebbit.backend.model.UserDocument;
import com.guebbit.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Map<String, Object> list(Integer pageInput, Integer pageSizeInput, String text, String id, String email, String username, Boolean active) {
        int page = PaginationUtil.page(pageInput);
        int pageSize = PaginationUtil.size(pageSizeInput);
        List<UserDocument> all = userRepository.findAll().stream().filter(u -> u.deletedAt == null).toList();
        List<UserDocument> filtered = all.stream().filter(u -> {
            if (id != null && !id.equals(u.id)) return false;
            if (email != null && !u.email.equalsIgnoreCase(email)) return false;
            if (username != null && (u.username == null || !u.username.toLowerCase().contains(username.toLowerCase()))) return false;
            if (active != null && active != u.active) return false;
            if (text != null && !text.isBlank()) {
                String t = text.toLowerCase();
                return (u.username != null && u.username.toLowerCase().contains(t)) || u.email.toLowerCase().contains(t);
            }
            return true;
        }).toList();

        int from = Math.min((page - 1) * pageSize, filtered.size());
        int to = Math.min(from + pageSize, filtered.size());
        List<Map<String, Object>> items = filtered.subList(from, to).stream().map(this::payload).toList();
        return Map.of("items", items, "meta", PaginationUtil.meta(page, pageSize, filtered.size()));
    }

    public Map<String, Object> create(String email, String username, String password, Boolean admin, Boolean active, String imageUrl) {
        userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(email).ifPresent(u -> { throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "EMAIL_ALREADY_EXISTS"); });
        UserDocument user = new UserDocument();
        user.email = email.toLowerCase();
        user.username = username;
        user.passwordHash = passwordEncoder.encode(password);
        user.admin = admin != null && admin;
        user.active = active == null || active;
        user.imageUrl = imageUrl;
        user.createdAt = Instant.now();
        user.updatedAt = Instant.now();
        userRepository.save(user);
        return payload(user);
    }

    public Map<String, Object> update(String id, String email, String username, String password, String imageUrl) {
        UserDocument user = userRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));
        if (user.deletedAt != null) throw new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND");
        if (email != null) user.email = email.toLowerCase();
        if (username != null) user.username = username;
        if (password != null) user.passwordHash = passwordEncoder.encode(password);
        if (imageUrl != null) user.imageUrl = imageUrl;
        user.updatedAt = Instant.now();
        userRepository.save(user);
        return payload(user);
    }

    public void delete(String id, Boolean hardDelete) {
        UserDocument user = userRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));
        if (Boolean.TRUE.equals(hardDelete)) {
            userRepository.deleteById(id);
        } else {
            user.deletedAt = Instant.now();
            user.updatedAt = Instant.now();
            userRepository.save(user);
        }
    }

    public Map<String, Object> getById(String id) {
        UserDocument user = userRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));
        if (user.deletedAt != null) throw new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND");
        return payload(user);
    }

    private Map<String, Object> payload(UserDocument user) {
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
