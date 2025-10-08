package com.example.evshop.domain.models;

public class ApiEnvelope<T> {
    public boolean isSuccess;
    public String message;
    public T result;
    public int statusCode;
}
