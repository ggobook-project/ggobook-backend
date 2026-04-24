package com.untitled.ggobook.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor // 프론트에서 온 JSON을 DTO로 변환하기 위해 필수!
public class UpdateMyInfoRequest {
    private String nickname;
    private String currentPassword; // 프론트에서 보내는 '현재 비밀번호'
    private String newPassword;     // 프론트에서 보내는 '새 비밀번호'
}