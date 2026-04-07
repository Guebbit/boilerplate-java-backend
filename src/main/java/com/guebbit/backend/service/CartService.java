package com.guebbit.backend.service;

import com.guebbit.backend.common.ApiException;
import com.guebbit.backend.model.ProductDocument;
import com.guebbit.backend.model.UserDocument;
import com.guebbit.backend.repository.UserRepository;
import com.guebbit.backend.security.AppPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
public class CartService {
    private final UserRepository userRepository;
    private final ProductService productService;
    private final OrderService orderService;

    public CartService(UserRepository userRepository, ProductService productService, OrderService orderService) {
        this.userRepository = userRepository;
        this.productService = productService;
        this.orderService = orderService;
    }

    public Map<String, Object> getCart(AppPrincipal principal) {
        UserDocument user = user(principal);
        return response(user);
    }

    public Map<String, Object> upsert(AppPrincipal principal, String productId, int quantity) {
        if (quantity < 1) throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "INVALID_QUANTITY");
        UserDocument user = user(principal);
        ProductDocument product = productService.requirePurchasable(productId);
        UserDocument.CartItem existing = user.cart.stream().filter(i -> productId.equals(i.productId)).findFirst().orElse(null);
        if (existing == null) {
            UserDocument.CartItem item = new UserDocument.CartItem();
            item.productId = product.id;
            item.title = product.title;
            item.price = product.price;
            item.quantity = quantity;
            user.cart.add(item);
        } else {
            existing.quantity = quantity;
            existing.price = product.price;
            existing.title = product.title;
        }
        user.updatedAt = Instant.now();
        userRepository.save(user);
        return response(user);
    }

    public Map<String, Object> remove(AppPrincipal principal, String productId) {
        UserDocument user = user(principal);
        if (productId == null) {
            user.cart.clear();
        } else {
            user.cart.removeIf(i -> productId.equals(i.productId));
        }
        user.updatedAt = Instant.now();
        userRepository.save(user);
        return response(user);
    }

    public Map<String, Object> summary(AppPrincipal principal) {
        UserDocument user = user(principal);
        return computeSummary(user);
    }

    public Map<String, Object> checkout(AppPrincipal principal, String email, String notes) {
        UserDocument user = user(principal);
        return orderService.checkout(user, email, notes);
    }

    private UserDocument user(AppPrincipal principal) {
        return userRepository.findById(principal.id()).orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "AUTH_REQUIRED"));
    }

    private Map<String, Object> response(UserDocument user) {
        return Map.of("items", user.cart, "summary", computeSummary(user));
    }

    private Map<String, Object> computeSummary(UserDocument user) {
        int itemsCount = user.cart.size();
        int totalQuantity = user.cart.stream().mapToInt(i -> i.quantity).sum();
        double total = user.cart.stream().mapToDouble(i -> i.price * i.quantity).sum();
        Map<String, Object> map = new HashMap<>();
        map.put("itemsCount", itemsCount);
        map.put("totalQuantity", totalQuantity);
        map.put("total", total);
        map.put("currency", "USD");
        return map;
    }
}
