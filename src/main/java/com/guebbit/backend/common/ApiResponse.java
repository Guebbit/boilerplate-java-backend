package com.guebbit.backend.common;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse(
        boolean success,
        int status,
        String message,
        Object data,
        List<String> errors
) {
    public static ApiResponse ok(int status, String message, Object data) {
        return new ApiResponse(true, status, message, data, null);
    }

    public static ApiResponse error(int status, String message, List<String> errors) {
        return new ApiResponse(false, status, message, null, errors);
    }
}
