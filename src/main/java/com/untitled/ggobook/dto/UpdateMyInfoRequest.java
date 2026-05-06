package com.untitled.ggobook.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateMyInfoRequest {
    private String nickname;
    private String currentPassword;

    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*])[a-zA-Z\\d!@#$%^&*]{8,16}$", message = "비밀번호는 영문, 숫자, 특수문자를 모두 포함하여 8~16자리로 입력해주세요.")
    private String newPassword;
}