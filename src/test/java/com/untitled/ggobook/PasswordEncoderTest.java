package com.untitled.ggobook;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordEncoderTest {

    @Test
    void generateAdminPassword() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hashedPassword = encoder.encode("admin"); // 'admin'을 해시로 변환
        System.out.println("==========================================");
        System.out.println("해시값: " + hashedPassword);
        System.out.println("==========================================");
    }
}
