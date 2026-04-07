package com.guebbit.backend.web;

import com.guebbit.backend.common.ApiResponse;
import com.guebbit.backend.config.AppProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitFilter extends OncePerRequestFilter {
    private final int limit;
    private final ObjectMapper objectMapper;
    private final Map<String, WindowCounter> counters = new ConcurrentHashMap<>();

    public RateLimitFilter(AppProperties properties, ObjectMapper objectMapper) {
        this.limit = properties.rateLimitPerMinute();
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String key = request.getRemoteAddr();
        WindowCounter counter = counters.computeIfAbsent(key, k -> new WindowCounter());
        if (Instant.now().isAfter(counter.windowStart.plusSeconds(60))) {
            counter.windowStart = Instant.now();
            counter.count.set(0);
        }
        if (counter.count.incrementAndGet() > limit) {
            response.setStatus(429);
            response.setContentType("application/json");
            objectMapper.writeValue(response.getOutputStream(), ApiResponse.error(429, "RATE_LIMITED", List.of("Too many requests")));
            return;
        }
        filterChain.doFilter(request, response);
    }

    static class WindowCounter {
        Instant windowStart = Instant.now();
        AtomicInteger count = new AtomicInteger(0);
    }
}
