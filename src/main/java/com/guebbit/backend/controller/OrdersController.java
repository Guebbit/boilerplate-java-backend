package com.guebbit.backend.controller;

import com.guebbit.backend.common.ApiResponse;
import com.guebbit.backend.common.SecurityUtils;
import com.guebbit.backend.security.AppPrincipal;
import com.guebbit.backend.service.OrderService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
public class OrdersController {
    private final OrderService orderService;

    public OrdersController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse> list(@RequestParam(required = false) String id,
                                            @RequestParam(required = false) Integer page,
                                            @RequestParam(required = false) Integer pageSize,
                                            @RequestParam(required = false) String userId,
                                            @RequestParam(required = false) String productId,
                                            @RequestParam(required = false) String email) {
        AppPrincipal principal = SecurityUtils.requirePrincipal();
        return ResponseEntity.ok(ApiResponse.ok(200, "ORDERS_OK", orderService.list(page, pageSize, id, userId, productId, email, principal)));
    }

    @PostMapping("/search")
    public ResponseEntity<ApiResponse> search(@RequestBody Map<String, Object> request) {
        AppPrincipal principal = SecurityUtils.requirePrincipal();
        return ResponseEntity.ok(ApiResponse.ok(200, "ORDERS_OK", orderService.list(
                (Integer) request.get("page"),
                (Integer) request.get("pageSize"),
                (String) request.get("id"),
                (String) request.get("userId"),
                (String) request.get("productId"),
                (String) request.get("email"),
                principal
        )));
    }

    @PostMapping
    public ResponseEntity<ApiResponse> create(@RequestBody Map<String, Object> request) {
        SecurityUtils.requireAdmin();
        return ResponseEntity.status(201).body(ApiResponse.ok(201, "ORDER_CREATED", orderService.create(
                (String) request.get("userId"),
                (String) request.get("email"),
                (List<Map<String, Object>>) request.get("items"),
                (String) request.get("notes")
        )));
    }

    @PutMapping
    public ResponseEntity<ApiResponse> update(@RequestBody Map<String, Object> request) {
        SecurityUtils.requireAdmin();
        return ResponseEntity.ok(ApiResponse.ok(200, "ORDER_UPDATED", orderService.update(
                (String) request.get("id"),
                (String) request.get("status"),
                (String) request.get("userId"),
                (String) request.get("email"),
                (List<Map<String, Object>>) request.get("items"),
                (String) request.get("notes")
        )));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse> delete(@RequestBody Map<String, Object> request) {
        SecurityUtils.requireAdmin();
        orderService.delete((String) request.get("id"));
        return ResponseEntity.ok(ApiResponse.ok(200, "ORDER_DELETED", Map.of()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getById(@PathVariable String id) {
        AppPrincipal principal = SecurityUtils.requirePrincipal();
        return ResponseEntity.ok(ApiResponse.ok(200, "ORDER_OK", orderService.getById(id, principal)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateById(@PathVariable String id, @RequestBody Map<String, Object> request) {
        SecurityUtils.requireAdmin();
        return ResponseEntity.ok(ApiResponse.ok(200, "ORDER_UPDATED", orderService.update(
                id,
                (String) request.get("status"),
                (String) request.get("userId"),
                (String) request.get("email"),
                (List<Map<String, Object>>) request.get("items"),
                (String) request.get("notes")
        )));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteById(@PathVariable String id) {
        SecurityUtils.requireAdmin();
        orderService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(200, "ORDER_DELETED", Map.of()));
    }

    @GetMapping("/{id}/invoice")
    public ResponseEntity<byte[]> invoice(@PathVariable String id) {
        AppPrincipal principal = SecurityUtils.requirePrincipal();
        byte[] bytes = orderService.invoicePdf(id, principal);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice-" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }
}
