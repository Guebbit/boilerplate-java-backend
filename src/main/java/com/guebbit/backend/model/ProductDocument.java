package com.guebbit.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("products")
public class ProductDocument {
    @Id
    public String id;

    @Indexed
    public String title;
    public double price;
    public String description;
    public boolean active = true;
    public String imageUrl;
    public Instant deletedAt;
    public Instant createdAt;
    public Instant updatedAt;
}
