package com.myow.service;

import java.util.ArrayList;
import java.util.List;

public class OperationResult<T> {
    private final boolean success;
    private final String message;
    private final T data;
    private final List<String> alertas;

    public OperationResult(boolean success, String message) {
        this(success, message, null, new ArrayList<>());
    }

    public OperationResult(boolean success, String message, T data) {
        this(success, message, data, new ArrayList<>());
    }

    public OperationResult(boolean success, String message, T data, List<String> alertas) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.alertas = alertas != null ? alertas : new ArrayList<>();
    }

    public static <T> OperationResult<T> ok(String message, T data) {
        return new OperationResult<>(true, message, data);
    }

    public static <T> OperationResult<T> ok(String message) {
        return new OperationResult<>(true, message);
    }

    public static <T> OperationResult<T> error(String message) {
        return new OperationResult<>(false, message);
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public T getData() { return data; }
    public List<String> getAlertas() { return alertas; }
}
