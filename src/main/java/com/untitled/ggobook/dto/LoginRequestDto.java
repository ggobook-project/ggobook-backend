package com.untitled.ggobook.dto; // 🌟 패키지가 dto로 변경됨!

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequestDto {
    private String userId;
    private String password;

    // 🌟 프론트에서 날아오는 로그인 상태 유지 체크 여부
    private boolean keepLoggedIn;
}