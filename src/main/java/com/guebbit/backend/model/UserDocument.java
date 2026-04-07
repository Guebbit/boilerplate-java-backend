package com.guebbit.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document("users")
public class UserDocument {
    @Id
    public String id;

    @Indexed(unique = true)
    public String email;
    public String username;
    public String passwordHash;
    public boolean admin;
    public boolean active = true;
    public String imageUrl;
    public Instant deletedAt;
    public Instant createdAt;
    public Instant updatedAt;
    public List<TokenEntry> tokens = new ArrayList<>();
    public List<CartItem> cart = new ArrayList<>();

    public static class TokenEntry {
        public String type;
        public String token;
        public Instant expiration;
    }

    public static class CartItem {
        public String productId;
        public String title;
        public double price;
        public int quantity;
    }
}
