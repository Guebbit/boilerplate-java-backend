package com.guebbit.backend.controller;

import com.guebbit.backend.common.ApiResponse;
import com.guebbit.backend.common.SecurityUtils;
import com.guebbit.backend.security.AppPrincipal;
import com.guebbit.backend.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/account")
public class AccountController {
    private final AuthService authService;

    public AccountController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getAccount() {
        AppPrincipal principal = SecurityUtils.requirePrincipal();
        return ResponseEntity.ok(ApiResponse.ok(200, "ACCOUNT_OK", authService.account(principal)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@RequestBody Map<String, Object> request) {
        Map<String, Object> tokens = authService.login((String) request.get("email"), (String) request.get("password"));
        return ResponseEntity.ok(ApiResponse.ok(200, "LOGIN_OK", tokens));
    }

    @PostMapping(value = "/signup", consumes = {"multipart/form-data"})
    public ResponseEntity<ApiResponse> signup(@RequestParam String email,
                                              @RequestParam String username,
                                              @RequestParam String password,
                                              @RequestParam String passwordConfirm,
                                              @RequestPart(required = false) MultipartFile imageUpload) {
        String imageUrl = imageUpload != null ? "/uploads/" + imageUpload.getOriginalFilename() : null;
        Map<String, Object> created = authService.signup(email, username, password, passwordConfirm, imageUrl);
        return ResponseEntity.status(201).body(ApiResponse.ok(201, "SIGNUP_OK", created));
    }

    @PostMapping("/reset")
    public ResponseEntity<ApiResponse> requestReset(@RequestBody Map<String, Object> request) {
        authService.requestPasswordReset((String) request.get("email"));
        return ResponseEntity.ok(ApiResponse.ok(200, "PASSWORD_RESET_REQUESTED", Map.of()));
    }

    @PostMapping("/reset-confirm")
    public ResponseEntity<ApiResponse> confirmReset(@RequestBody Map<String, Object> request) {
        authService.confirmPasswordReset((String) request.get("token"), (String) request.get("password"), (String) request.get("passwordConfirm"));
        return ResponseEntity.ok(ApiResponse.ok(200, "PASSWORD_RESET_CONFIRMED", Map.of()));
    }

    @GetMapping("/refresh")
    public ResponseEntity<ApiResponse> refresh(@CookieValue(name = "jwt", required = false) String cookieToken,
                                               @RequestParam(name = "token", required = false) String queryToken) {
        String token = queryToken != null ? queryToken : cookieToken;
        Map<String, Object> refreshed = authService.refresh(token);
        return ResponseEntity.ok(ApiResponse.ok(200, "REFRESH_OK", refreshed));
    }

    @GetMapping("/refresh/{token}")
    public ResponseEntity<ApiResponse> refreshByPath(@PathVariable String token) {
        Map<String, Object> refreshed = authService.refresh(token);
        return ResponseEntity.ok(ApiResponse.ok(200, "REFRESH_OK", refreshed));
    }

    @PostMapping("/logout-all")
    public ResponseEntity<ApiResponse> logoutAll() {
        AppPrincipal principal = SecurityUtils.requirePrincipal();
        authService.logoutAll(principal);
        return ResponseEntity.ok(ApiResponse.ok(200, "LOGOUT_ALL_OK", Map.of()));
    }

    @DeleteMapping("/tokens/expired")
    public ResponseEntity<ApiResponse> deleteExpired() {
        SecurityUtils.requireAdmin();
        long removed = authService.deleteExpiredTokens();
        return ResponseEntity.ok(ApiResponse.ok(200, "EXPIRED_TOKENS_CLEANED", Map.of("updatedUsers", removed)));
    }

}
