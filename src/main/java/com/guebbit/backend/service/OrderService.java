package com.guebbit.backend.service;

import com.guebbit.backend.common.ApiException;
import com.guebbit.backend.model.OrderDocument;
import com.guebbit.backend.model.UserDocument;
import com.guebbit.backend.repository.OrderRepository;
import com.guebbit.backend.repository.UserRepository;
import com.guebbit.backend.security.AppPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public OrderService(OrderRepository orderRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    public Map<String, Object> list(Integer pageInput, Integer pageSizeInput, String id, String userId, String productId, String email, AppPrincipal principal) {
        int page = PaginationUtil.page(pageInput);
        int pageSize = PaginationUtil.size(pageSizeInput);
        List<OrderDocument> filtered = orderRepository.findAll().stream().filter(o -> {
            if (!principal.admin() && !o.userId.equals(principal.id())) return false;
            if (id != null && !id.equals(o.id)) return false;
            if (userId != null && !userId.equals(o.userId)) return false;
            if (email != null && !email.equalsIgnoreCase(o.email)) return false;
            if (productId != null && o.items.stream().noneMatch(i -> productId.equals(i.productId))) return false;
            return true;
        }).toList();

        int from = Math.min((page - 1) * pageSize, filtered.size());
        int to = Math.min(from + pageSize, filtered.size());
        List<Map<String, Object>> items = filtered.subList(from, to).stream().map(this::payload).toList();
        return Map.of("items", items, "meta", PaginationUtil.meta(page, pageSize, filtered.size()));
    }

    public Map<String, Object> getById(String id, AppPrincipal principal) {
        OrderDocument order = orderRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND"));
        if (!principal.admin() && !order.userId.equals(principal.id())) throw new ApiException(HttpStatus.FORBIDDEN, "ORDER_SCOPE_FORBIDDEN");
        return payload(order);
    }

    public Map<String, Object> create(String userId, String email, List<Map<String, Object>> items, String notes) {
        userRepository.findById(userId).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));
        if (items == null || items.isEmpty()) throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "ORDER_ITEMS_REQUIRED");
        OrderDocument order = new OrderDocument();
        order.userId = userId;
        order.email = email;
        order.notes = notes;
        order.status = "pending";
        order.createdAt = Instant.now();
        order.updatedAt = Instant.now();
        order.items = items.stream().map(this::toOrderItem).toList();
        order.total = order.items.stream().mapToDouble(i -> i.price * i.quantity).sum();
        orderRepository.save(order);
        return payload(order);
    }

    public Map<String, Object> update(String id, String status, String userId, String email, List<Map<String, Object>> items, String notes) {
        OrderDocument order = orderRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND"));
        if (status != null) order.status = status;
        if (userId != null) order.userId = userId;
        if (email != null) order.email = email;
        if (notes != null) order.notes = notes;
        if (items != null && !items.isEmpty()) order.items = items.stream().map(this::toOrderItem).toList();
        order.total = order.items.stream().mapToDouble(i -> i.price * i.quantity).sum();
        order.updatedAt = Instant.now();
        orderRepository.save(order);
        return payload(order);
    }

    public void delete(String id) {
        OrderDocument order = orderRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND"));
        orderRepository.delete(order);
    }

    public byte[] invoicePdf(String id, AppPrincipal principal) {
        OrderDocument order = orderRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND"));
        if (!principal.admin() && !order.userId.equals(principal.id())) throw new ApiException(HttpStatus.FORBIDDEN, "ORDER_SCOPE_FORBIDDEN");
        String content = "Invoice for order " + order.id + "\nTotal: " + order.total + "\nStatus: " + order.status;
        return content.getBytes(StandardCharsets.UTF_8);
    }

    public Map<String, Object> checkout(UserDocument user, String emailOverride, String notes) {
        if (user.cart == null || user.cart.isEmpty()) throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "CART_EMPTY");
        OrderDocument order = new OrderDocument();
        order.userId = user.id;
        order.email = emailOverride != null ? emailOverride : user.email;
        order.status = "pending";
        order.notes = notes;
        order.createdAt = Instant.now();
        order.updatedAt = Instant.now();
        order.items = user.cart.stream().map(c -> {
            OrderDocument.OrderItem i = new OrderDocument.OrderItem();
            i.productId = c.productId;
            i.title = c.title;
            i.price = c.price;
            i.quantity = c.quantity;
            return i;
        }).toList();
        order.total = order.items.stream().mapToDouble(i -> i.price * i.quantity).sum();
        orderRepository.save(order);
        user.cart.clear();
        user.updatedAt = Instant.now();
        userRepository.save(user);
        return Map.of("order", payload(order), "message", "CHECKOUT_SUCCESS");
    }

    private OrderDocument.OrderItem toOrderItem(Map<String, Object> source) {
        OrderDocument.OrderItem item = new OrderDocument.OrderItem();
        item.productId = (String) source.get("productId");
        item.title = (String) source.getOrDefault("title", "");
        item.price = source.get("price") == null ? 0D : ((Number) source.get("price")).doubleValue();
        item.quantity = source.get("quantity") == null ? 1 : ((Number) source.get("quantity")).intValue();
        return item;
    }

    private Map<String, Object> payload(OrderDocument order) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", order.id);
        map.put("userId", order.userId);
        map.put("email", order.email);
        map.put("items", order.items);
        map.put("total", order.total);
        map.put("notes", order.notes);
        map.put("status", order.status);
        map.put("createdAt", order.createdAt);
        map.put("updatedAt", order.updatedAt);
        return map;
    }
}
