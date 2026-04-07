package com.guebbit.backend.security;

public record AppPrincipal(String id, String email, boolean admin) {
}
