package com.powergrid.ticketsystem.dto;

import java.time.LocalDateTime;

/**
 * ============================================================
 * GENERIC API RESPONSE DTO
 * ============================================================
 * 
 * Standard wrapper for all API responses in the system.
 * Provides consistent response structure across all endpoints.
 * 
 * RESPONSE STRUCTURE:
 * {
 * "success": true/false,
 * "message": "Operation status message",
 * "data": { actual response data },
 * "timestamp": "2024-01-15T10:30:00"
 * }
 * 
 * @param <T> Type of data contained in the response
 */
public class ApiResponse<T> {

    /**
     * Indicates if the operation was successful.
     */
    private boolean success;

    /**
     * Human-readable message about the operation result.
     */
    private String message;

    /**
     * Actual data payload of the response.
     * Can be a single object, list, or null.
     */
    private T data;

    /**
     * Timestamp when the response was generated.
     */
    private LocalDateTime timestamp;

    /**
     * Total count of items (useful for list responses).
     */
    private Integer totalCount;

    // ============================================================
    // CONSTRUCTORS
    // ============================================================

    /**
     * Default constructor.
     */
    public ApiResponse() {
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Constructor for simple success/failure response without data.
     * 
     * @param success Operation success status
     * @param message Response message
     */
    public ApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Constructor for response with data.
     * 
     * @param success Operation success status
     * @param message Response message
     * @param data    Response data payload
     */
    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Constructor for list response with count.
     * 
     * @param success    Operation success status
     * @param message    Response message
     * @param data       Response data payload
     * @param totalCount Total number of items
     */
    public ApiResponse(boolean success, String message, T data, Integer totalCount) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.totalCount = totalCount;
        this.timestamp = LocalDateTime.now();
    }

    // ============================================================
    // STATIC FACTORY METHODS (for cleaner code)
    // ============================================================

    /**
     * Creates a success response with data.
     * 
     * @param message Success message
     * @param data    Response data
     * @return ApiResponse with success=true
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    /**
     * Creates a success response with data and count.
     * 
     * @param message Success message
     * @param data    Response data
     * @param count   Total count
     * @return ApiResponse with success=true
     */
    public static <T> ApiResponse<T> success(String message, T data, int count) {
        return new ApiResponse<>(true, message, data, count);
    }

    /**
     * Creates an error response.
     * 
     * @param message Error message
     * @return ApiResponse with success=false
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message);
    }

    // ============================================================
    // GETTERS AND SETTERS
    // ============================================================

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }
}
