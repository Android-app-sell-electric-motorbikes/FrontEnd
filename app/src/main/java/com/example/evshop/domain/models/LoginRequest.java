package com.example.evshop.domain.models;

public class LoginRequest {
    public String email;
    public String password;
    public boolean rememberMe;

    public LoginRequest(String email, String password, boolean rememberMe){
        this.email = email;
        this.password = password;
        this.rememberMe = rememberMe;
    }
}
