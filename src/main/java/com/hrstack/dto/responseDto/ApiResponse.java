package com.hrstack.dto.responseDto;

import lombok.Data;

@Data
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;

    private ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse <T> success(boolean success, String message, T data){
        return new ApiResponse<>(success, message, data);
    }
    public static <T> ApiResponse <T> success( T data){
        return new ApiResponse<>(true, "Login successful", data);
    }
}

