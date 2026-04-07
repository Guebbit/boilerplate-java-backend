package com.guebbit.backend.service;

import com.guebbit.backend.common.ApiException;
import com.guebbit.backend.model.UserDocument;
import com.guebbit.backend.repository.UserRepository;
import com.guebbit.backend.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void loginFailsForUnknownUser() {
        when(userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull("none@example.com")).thenReturn(Optional.empty());
        assertThrows(ApiException.class, () -> authService.login("none@example.com", "password123"));
    }

    @Test
    void signupRejectsMismatchedPasswords() {
        assertThrows(ApiException.class, () -> authService.signup("x@y.com", "user", "a", "b", null));
    }
}
