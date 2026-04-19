package com.untitled.ggobook.domain;

import lombok.Data;

@Data
public class LoginRequest {
    private String userId;
    private String password;
}