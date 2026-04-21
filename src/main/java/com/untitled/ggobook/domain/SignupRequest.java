package com.untitled.ggobook.domain; // 🌟 팀 규칙에 맞게 domain으로 완벽 통일!

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequest {

    @NotBlank(message = "아이디를 입력해주세요.")
    @Size(min = 4, max = 20, message = "아이디는 4자 이상, 20자 이하로 입력해주세요.")
    private String userId;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Size(min = 8, max = 20, message = "비밀번호는 8자 이상, 20자 이하로 입력해주세요.")
    private String password;

    @NotBlank(message = "이름을 입력해주세요.")
    private String name;

    @NotBlank(message = "닉네임을 입력해주세요.")
    @Size(min = 2, max = 10, message = "닉네임은 2자 이상, 10자 이하로 입력해주세요.")
    private String nickname;

    @NotBlank(message = "이메일을 입력해주세요.")
    private String email;

    @NotBlank(message = "성별을 선택해주세요.")
    private String gender;
}