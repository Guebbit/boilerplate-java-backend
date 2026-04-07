package com.guebbit.backend.repository;

import com.guebbit.backend.model.OrderDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrderRepository extends MongoRepository<OrderDocument, String> {
}
