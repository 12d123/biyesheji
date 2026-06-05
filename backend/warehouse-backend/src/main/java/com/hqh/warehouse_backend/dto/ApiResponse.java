package com.hqh.warehouse_backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 通用API响应数据传输对象
 */
public class ApiResponse<T> {
    
    private Boolean success;
    private String message;
    private T data;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String timestamp;
    
    private String path;
    
    // ========== 构造函数 ==========
    
    public ApiResponse() {
        this.timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
    
    public ApiResponse(Boolean success, String message) {
        this();
        this.success = success;
        this.message = message;
    }
    
    public ApiResponse(Boolean success, String message, T data) {
        this();
        this.success = success;
        this.message = message;
        this.data = data;
    }
    
    // ========== Getter 和 Setter ==========
    
    public Boolean getSuccess() {
        return success;
    }
    
    public void setSuccess(Boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public T getData() {
        return data;
    }
    
    public void setData(T data) {
        this.data = data;
    }
    
    public String getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
    
    // ========== 静态工厂方法 ==========
    
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "操作成功", data);
    }
    
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }
    
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message);
    }
    
    public static <T> ApiResponse<T> error(String message, T data) {
        return new ApiResponse<>(false, message, data);
    }
    
    public static <T> ApiResponse<T> of(Boolean success, String message, T data) {
        return new ApiResponse<>(success, message, data);
    }
    
    @Override
    public String toString() {
        return String.format("ApiResponse{success=%s, message='%s', data=%s, timestamp=%s}", 
                success, message, data, timestamp);
    }
}