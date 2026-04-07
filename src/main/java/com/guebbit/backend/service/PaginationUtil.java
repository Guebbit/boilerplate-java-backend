package com.guebbit.backend.service;

import java.util.Map;

public final class PaginationUtil {
    private PaginationUtil() {}

    public static int page(Integer page) {
        return page == null || page < 1 ? 1 : page;
    }

    public static int size(Integer pageSize) {
        if (pageSize == null || pageSize < 1) return 10;
        return Math.min(pageSize, 100);
    }

    public static Map<String, Object> meta(int page, int pageSize, long totalItems) {
        long totalPages = (long) Math.ceil(totalItems / (double) pageSize);
        return Map.of(
                "page", page,
                "pageSize", pageSize,
                "totalItems", totalItems,
                "totalPages", totalPages
        );
    }
}
