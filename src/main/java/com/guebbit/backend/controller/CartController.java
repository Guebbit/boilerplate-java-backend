package com.guebbit.backend.controller;

import com.guebbit.backend.common.ApiResponse;
import com.guebbit.backend.common.SecurityUtils;
import com.guebbit.backend.security.AppPrincipal;
import com.guebbit.backend.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/cart")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getCart() {
        AppPrincipal principal = SecurityUtils.requirePrincipal();
        return ResponseEntity.ok(ApiResponse.ok(200, "CART_OK", cartService.getCart(principal)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse> upsert(@RequestBody Map<String, Object> request) {
        AppPrincipal principal = SecurityUtils.requirePrincipal();
        return ResponseEntity.ok(ApiResponse.ok(200, "CART_UPDATED", cartService.upsert(principal,
                (String) request.get("productId"),
                ((Number) request.get("quantity")).intValue())));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse> clearOrRemove(@RequestBody(required = false) Map<String, Object> request) {
        AppPrincipal principal = SecurityUtils.requirePrincipal();
        String productId = request == null ? null : (String) request.get("productId");
        return ResponseEntity.ok(ApiResponse.ok(200, "CART_UPDATED", cartService.remove(principal, productId)));
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ApiResponse> updateByProduct(@PathVariable String productId, @RequestBody Map<String, Object> request) {
        AppPrincipal principal = SecurityUtils.requirePrincipal();
        return ResponseEntity.ok(ApiResponse.ok(200, "CART_UPDATED", cartService.upsert(principal, productId,
                ((Number) request.get("quantity")).intValue())));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse> removeByProduct(@PathVariable String productId) {
        AppPrincipal principal = SecurityUtils.requirePrincipal();
        return ResponseEntity.ok(ApiResponse.ok(200, "CART_UPDATED", cartService.remove(principal, productId)));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse> summary() {
        AppPrincipal principal = SecurityUtils.requirePrincipal();
        return ResponseEntity.ok(ApiResponse.ok(200, "CART_SUMMARY_OK", cartService.summary(principal)));
    }

    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse> checkout(@RequestBody(required = false) Map<String, Object> request) {
        AppPrincipal principal = SecurityUtils.requirePrincipal();
        String email = request == null ? null : (String) request.get("email");
        String notes = request == null ? null : (String) request.get("notes");
        return ResponseEntity.status(201).body(ApiResponse.ok(201, "CHECKOUT_OK", cartService.checkout(principal, email, notes)));
    }
}
