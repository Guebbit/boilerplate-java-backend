package com.guebbit.backend.service;

import com.guebbit.backend.common.ApiException;
import com.guebbit.backend.model.ProductDocument;
import com.guebbit.backend.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Map<String, Object> list(Integer pageInput, Integer pageSizeInput, String text, String id, Double minPrice, Double maxPrice, boolean admin) {
        int page = PaginationUtil.page(pageInput);
        int pageSize = PaginationUtil.size(pageSizeInput);
        List<ProductDocument> filtered = productRepository.findAll().stream().filter(p -> {
            if (id != null && !id.equals(p.id)) return false;
            if (text != null && !text.isBlank()) {
                String t = text.toLowerCase();
                if ((p.title == null || !p.title.toLowerCase().contains(t)) && (p.description == null || !p.description.toLowerCase().contains(t))) return false;
            }
            if (minPrice != null && p.price < minPrice) return false;
            if (maxPrice != null && p.price > maxPrice) return false;
            if (!admin && (!p.active || p.deletedAt != null)) return false;
            return admin || p.deletedAt == null;
        }).toList();

        int from = Math.min((page - 1) * pageSize, filtered.size());
        int to = Math.min(from + pageSize, filtered.size());
        List<Map<String, Object>> items = filtered.subList(from, to).stream().map(this::payload).toList();
        return Map.of("items", items, "meta", PaginationUtil.meta(page, pageSize, filtered.size()));
    }

    public Map<String, Object> getById(String id, boolean admin) {
        ProductDocument p = productRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND"));
        if (!admin && (!p.active || p.deletedAt != null)) throw new ApiException(HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND");
        return payload(p);
    }

    public Map<String, Object> create(String title, double price, String description, Boolean active, String imageUrl) {
        ProductDocument p = new ProductDocument();
        p.title = title;
        p.price = price;
        p.description = description;
        p.active = active == null || active;
        p.imageUrl = imageUrl;
        p.createdAt = Instant.now();
        p.updatedAt = Instant.now();
        productRepository.save(p);
        return payload(p);
    }

    public Map<String, Object> update(String id, String title, Double price, String description, Boolean active, String imageUrl) {
        ProductDocument p = productRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND"));
        if (title != null) p.title = title;
        if (price != null) p.price = price;
        if (description != null) p.description = description;
        if (active != null) p.active = active;
        if (imageUrl != null) p.imageUrl = imageUrl;
        p.updatedAt = Instant.now();
        productRepository.save(p);
        return payload(p);
    }

    public void delete(String id, Boolean hardDelete) {
        ProductDocument p = productRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND"));
        if (Boolean.TRUE.equals(hardDelete)) {
            productRepository.deleteById(id);
        } else {
            p.deletedAt = Instant.now();
            p.updatedAt = Instant.now();
            productRepository.save(p);
        }
    }

    public ProductDocument requirePurchasable(String id) {
        ProductDocument p = productRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND"));
        if (!p.active || p.deletedAt != null) throw new ApiException(HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND");
        return p;
    }

    private Map<String, Object> payload(ProductDocument p) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", p.id);
        map.put("title", p.title);
        map.put("price", p.price);
        map.put("description", p.description);
        map.put("active", p.active);
        map.put("imageUrl", p.imageUrl);
        map.put("createdAt", p.createdAt);
        map.put("updatedAt", p.updatedAt);
        map.put("deletedAt", p.deletedAt);
        return map;
    }
}
