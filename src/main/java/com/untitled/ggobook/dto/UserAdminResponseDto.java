package com.untitled.ggobook.dto;

import com.untitled.ggobook.domain.User;
import com.untitled.ggobook.domain.enums.UserStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UserAdminResponseDto {
    private Long id;
    private String userId;
    private String nickname;
    private String name;
    private String email;
    private UserStatus status;
    private LocalDateTime suspensionEndDate;

    private String profileImageUrl;

    public UserAdminResponseDto(User user) {
        this.id = user.getId();
        this.userId = user.getUserId();
        this.nickname = user.getNickname();
        this.name = user.getName();
        this.email = user.getEmail();
        this.status = user.getStatus();
        this.suspensionEndDate = user.getSuspensionEndDate();
        this.profileImageUrl = user.getProfileImageUrl();
    }
}