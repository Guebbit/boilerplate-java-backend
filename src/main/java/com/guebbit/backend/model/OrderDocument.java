package com.guebbit.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document("orders")
public class OrderDocument {
    @Id
    public String id;
    public String userId;
    public String email;
    public List<OrderItem> items = new ArrayList<>();
    public double total;
    public String notes;
    public String status = "pending";
    public Instant createdAt;
    public Instant updatedAt;

    public static class OrderItem {
        public String productId;
        public String title;
        public double price;
        public int quantity;
    }
}
