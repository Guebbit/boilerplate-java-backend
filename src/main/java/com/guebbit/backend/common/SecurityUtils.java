package com.guebbit.backend.common;

import com.guebbit.backend.security.AppPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {
    private SecurityUtils() {}

    public static AppPrincipal requirePrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof AppPrincipal principal)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "AUTH_REQUIRED");
        }
        return principal;
    }

    public static void requireAdmin() {
        if (!requirePrincipal().admin()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "ADMIN_REQUIRED");
        }
    }
}
