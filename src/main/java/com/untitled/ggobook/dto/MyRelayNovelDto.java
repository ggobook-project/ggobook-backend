package com.untitled.ggobook.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MyRelayNovelDto {
    private Long id;              // 릴레이 소설 방 번호 (RelayNovelId)
    private String title;         // 소설 제목
    private String role;          // "시작자" or "참여자"
    private int entries;          // 내 이어쓰기 횟수
    private int participants;     // 총 참여자 수 (중복 제거)
    private String date;          // 2026.04.10 형식의 참여일
}