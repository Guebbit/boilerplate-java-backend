package com.guebbit.backend.service;

import com.guebbit.backend.common.ApiException;
import com.guebbit.backend.model.ProductDocument;
import com.guebbit.backend.model.UserDocument;
import com.guebbit.backend.repository.UserRepository;
import com.guebbit.backend.security.AppPrincipal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProductService productService;
    @Mock
    private OrderService orderService;

    @InjectMocks
    private CartService cartService;

    @Test
    void upsertRejectsInvalidQuantity() {
        AppPrincipal principal = new AppPrincipal("u1", "a@b.com", false);
        assertThrows(ApiException.class, () -> cartService.upsert(principal, "p1", 0));
    }

    @Test
    void upsertAddsCartLine() {
        AppPrincipal principal = new AppPrincipal("u1", "a@b.com", false);
        UserDocument user = new UserDocument();
        user.id = "u1";

        ProductDocument product = new ProductDocument();
        product.id = "p1";
        product.title = "Item";
        product.price = 12.5;
        product.active = true;

        when(userRepository.findById("u1")).thenReturn(Optional.of(user));
        when(productService.requirePurchasable("p1")).thenReturn(product);
        when(userRepository.save(any(UserDocument.class))).thenAnswer(i -> i.getArgument(0));

        Map<String, Object> response = cartService.upsert(principal, "p1", 2);
        assertEquals(1, ((java.util.List<?>) response.get("items")).size());
        verify(userRepository, times(1)).save(any(UserDocument.class));
    }
}
