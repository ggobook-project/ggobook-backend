package com.untitled.ggobook.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String userId;

    // 암호화된 비밀번호(60자)를 담기 위해 100자로 넉넉하게 세팅
    @Column(nullable = false, length = 100)
    private String password;

    @Column(nullable = false, length = 20)
    private String name;

    @Column(unique = true, nullable = false, length = 50)
    private String nickname;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(length = 10)
    private String gender;

    @Column(length = 20)
    private String role;

    // ==========================================
    // 소셜 로그인 정보 갱신용
    // ==========================================
    public User update(String name, String email) {
        this.name = name;
        this.email = email;
        return this;
    }
}