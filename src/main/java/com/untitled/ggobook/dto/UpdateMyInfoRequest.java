package com.untitled.ggobook.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

//정보 수정 요청용
@Getter
@NoArgsConstructor
public class UpdateMyInfoRequest {
    private String nickname;
    private String password; // 변경할 비밀번호 (null이거나 비어있으면 변경 안 함)
}