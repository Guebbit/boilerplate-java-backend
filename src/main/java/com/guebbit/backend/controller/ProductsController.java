package com.guebbit.backend.controller;

import com.guebbit.backend.common.ApiResponse;
import com.guebbit.backend.common.SecurityUtils;
import com.guebbit.backend.security.AppPrincipal;
import com.guebbit.backend.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/products")
public class ProductsController {
    private final ProductService productService;

    public ProductsController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse> list(@RequestParam(required = false) String id,
                                            @RequestParam(required = false) Integer page,
                                            @RequestParam(required = false) Integer pageSize,
                                            @RequestParam(required = false) String text,
                                            @RequestParam(required = false) Double minPrice,
                                            @RequestParam(required = false) Double maxPrice,
                                            Authentication auth) {
        boolean admin = auth != null && auth.getPrincipal() instanceof AppPrincipal p && p.admin();
        return ResponseEntity.ok(ApiResponse.ok(200, "PRODUCTS_OK", productService.list(page, pageSize, text, id, minPrice, maxPrice, admin)));
    }

    @PostMapping("/search")
    public ResponseEntity<ApiResponse> search(@RequestBody Map<String, Object> request, Authentication auth) {
        boolean admin = auth != null && auth.getPrincipal() instanceof AppPrincipal p && p.admin();
        return ResponseEntity.ok(ApiResponse.ok(200, "PRODUCTS_OK", productService.list(
                (Integer) request.get("page"),
                (Integer) request.get("pageSize"),
                (String) request.get("text"),
                (String) request.get("id"),
                request.get("minPrice") == null ? null : ((Number) request.get("minPrice")).doubleValue(),
                request.get("maxPrice") == null ? null : ((Number) request.get("maxPrice")).doubleValue(),
                admin
        )));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getById(@PathVariable String id, Authentication auth) {
        boolean admin = auth != null && auth.getPrincipal() instanceof AppPrincipal p && p.admin();
        return ResponseEntity.ok(ApiResponse.ok(200, "PRODUCT_OK", productService.getById(id, admin)));
    }

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<ApiResponse> create(@RequestParam String title,
                                              @RequestParam double price,
                                              @RequestParam(required = false) String description,
                                              @RequestParam(required = false) Boolean active,
                                              @RequestPart(required = false) MultipartFile imageUpload) {
        SecurityUtils.requireAdmin();
        String imageUrl = imageUpload != null ? "/uploads/" + imageUpload.getOriginalFilename() : null;
        return ResponseEntity.status(201).body(ApiResponse.ok(201, "PRODUCT_CREATED", productService.create(title, price, description, active, imageUrl)));
    }

    @PutMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<ApiResponse> update(@RequestParam String id,
                                              @RequestParam(required = false) String title,
                                              @RequestParam(required = false) Double price,
                                              @RequestParam(required = false) String description,
                                              @RequestParam(required = false) Boolean active,
                                              @RequestPart(required = false) MultipartFile imageUpload) {
        SecurityUtils.requireAdmin();
        String imageUrl = imageUpload != null ? "/uploads/" + imageUpload.getOriginalFilename() : null;
        return ResponseEntity.ok(ApiResponse.ok(200, "PRODUCT_UPDATED", productService.update(id, title, price, description, active, imageUrl)));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse> delete(@RequestBody Map<String, Object> request) {
        SecurityUtils.requireAdmin();
        productService.delete((String) request.get("id"), (Boolean) request.get("hardDelete"));
        return ResponseEntity.ok(ApiResponse.ok(200, "PRODUCT_DELETED", Map.of()));
    }

    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<ApiResponse> updateById(@PathVariable String id,
                                                  @RequestParam(required = false) String title,
                                                  @RequestParam(required = false) Double price,
                                                  @RequestParam(required = false) String description,
                                                  @RequestParam(required = false) Boolean active,
                                                  @RequestPart(required = false) MultipartFile imageUpload) {
        SecurityUtils.requireAdmin();
        String imageUrl = imageUpload != null ? "/uploads/" + imageUpload.getOriginalFilename() : null;
        return ResponseEntity.ok(ApiResponse.ok(200, "PRODUCT_UPDATED", productService.update(id, title, price, description, active, imageUrl)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteById(@PathVariable String id,
                                                  @RequestParam(required = false) Boolean hardDelete) {
        SecurityUtils.requireAdmin();
        productService.delete(id, hardDelete);
        return ResponseEntity.ok(ApiResponse.ok(200, "PRODUCT_DELETED", Map.of()));
    }
}
