package com.guebbit.backend.repository;

import com.guebbit.backend.model.UserDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<UserDocument, String> {
    Optional<UserDocument> findByEmailIgnoreCaseAndDeletedAtIsNull(String email);
}
