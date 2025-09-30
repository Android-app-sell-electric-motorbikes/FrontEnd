package com.example.evshop.models;

public class ApiEnvelope<T> {
    public boolean isSuccess;
    public String message;
    public T result;
    public int statusCode;
}
