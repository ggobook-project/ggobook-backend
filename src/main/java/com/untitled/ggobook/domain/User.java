package com.untitled.ggobook.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String userId; // 아이디

    @Column(nullable = false)
    private String password; // 비밀번호

    // 🌟 추가된 기둥들 🌟
    @Column(nullable = false, length = 50)
    private String name; // 이름 (실명)

    @Column(nullable = false, unique = true, length = 50)
    private String email; // 이메일 (비밀번호 찾기용)

    @Column(nullable = false, unique = true, length = 50)
    private String nickname; // 닉네임 (커뮤니티 활동용)

    @Column(nullable = false, length = 20)
    private String role; // 권한 (USER, ADMIN)
}