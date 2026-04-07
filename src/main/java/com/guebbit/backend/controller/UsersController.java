package com.guebbit.backend.controller;

import com.guebbit.backend.common.ApiResponse;
import com.guebbit.backend.common.SecurityUtils;
import com.guebbit.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/users")
public class UsersController {
    private final UserService userService;

    public UsersController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse> list(@RequestParam(required = false) Integer page,
                                            @RequestParam(required = false) Integer pageSize,
                                            @RequestParam(required = false) String text,
                                            @RequestParam(required = false) String id,
                                            @RequestParam(required = false) String email,
                                            @RequestParam(required = false) String username,
                                            @RequestParam(required = false) Boolean active) {
        SecurityUtils.requireAdmin();
        return ResponseEntity.ok(ApiResponse.ok(200, "USERS_OK", userService.list(page, pageSize, text, id, email, username, active)));
    }

    @PostMapping("/search")
    public ResponseEntity<ApiResponse> search(@RequestBody Map<String, Object> request) {
        SecurityUtils.requireAdmin();
        return ResponseEntity.ok(ApiResponse.ok(200, "USERS_OK", userService.list(
                (Integer) request.get("page"),
                (Integer) request.get("pageSize"),
                (String) request.get("text"),
                (String) request.get("id"),
                (String) request.get("email"),
                (String) request.get("username"),
                (Boolean) request.get("active")
        )));
    }

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<ApiResponse> create(@RequestParam String email,
                                              @RequestParam String username,
                                              @RequestParam String password,
                                              @RequestParam(required = false) Boolean admin,
                                              @RequestParam(required = false) Boolean active,
                                              @RequestPart(required = false) MultipartFile imageUpload) {
        SecurityUtils.requireAdmin();
        String imageUrl = imageUpload != null ? "/uploads/" + imageUpload.getOriginalFilename() : null;
        return ResponseEntity.status(201).body(ApiResponse.ok(201, "USER_CREATED", userService.create(email, username, password, admin, active, imageUrl)));
    }

    @PutMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<ApiResponse> update(@RequestParam String id,
                                              @RequestParam(required = false) String email,
                                              @RequestParam(required = false) String username,
                                              @RequestParam(required = false) String password,
                                              @RequestPart(required = false) MultipartFile imageUpload) {
        SecurityUtils.requireAdmin();
        String imageUrl = imageUpload != null ? "/uploads/" + imageUpload.getOriginalFilename() : null;
        return ResponseEntity.ok(ApiResponse.ok(200, "USER_UPDATED", userService.update(id, email, username, password, imageUrl)));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse> delete(@RequestBody Map<String, Object> request) {
        SecurityUtils.requireAdmin();
        userService.delete((String) request.get("id"), (Boolean) request.get("hardDelete"));
        return ResponseEntity.ok(ApiResponse.ok(200, "USER_DELETED", Map.of()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getById(@PathVariable String id) {
        SecurityUtils.requireAdmin();
        return ResponseEntity.ok(ApiResponse.ok(200, "USER_OK", userService.getById(id)));
    }

    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<ApiResponse> updateById(@PathVariable String id,
                                                  @RequestParam(required = false) String email,
                                                  @RequestParam(required = false) String username,
                                                  @RequestParam(required = false) String password,
                                                  @RequestPart(required = false) MultipartFile imageUpload) {
        SecurityUtils.requireAdmin();
        String imageUrl = imageUpload != null ? "/uploads/" + imageUpload.getOriginalFilename() : null;
        return ResponseEntity.ok(ApiResponse.ok(200, "USER_UPDATED", userService.update(id, email, username, password, imageUrl)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteById(@PathVariable String id,
                                                  @RequestParam(required = false) Boolean hardDelete) {
        SecurityUtils.requireAdmin();
        userService.delete(id, hardDelete);
        return ResponseEntity.ok(ApiResponse.ok(200, "USER_DELETED", Map.of()));
    }
}
