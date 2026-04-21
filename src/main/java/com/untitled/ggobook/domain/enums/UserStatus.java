package com.untitled.ggobook.domain.enums;

public enum UserStatus {
    ACTIVE("정상 활동중"),
    SUSPENDED("이용 정지됨"),
    WITHDRAWN("탈퇴한 회원");

    private final String description;

    UserStatus(String description) { this.description = description; }

    public String getDescription() { return description; }
}