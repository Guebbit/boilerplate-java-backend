package com.guebbit.backend.repository;

import com.guebbit.backend.model.ProductDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductRepository extends MongoRepository<ProductDocument, String> {
}
