package com.untitled.ggobook.dto;

import com.untitled.ggobook.domain.Point;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PointHistoryDto {
    private String pointType; // "CHARGE" 또는 "DEDUCT"
    private Integer amount;   // 금액
    private String description; // "카카오페이 충전", "나혼자만 레벨업 3화 열람" 등
    private LocalDateTime createdAt;

    public static PointHistoryDto from(Point point) {
        return PointHistoryDto.builder()
                .pointType(point.getPointType())
                .amount(point.getAmount())
                .description(point.getDescription())
                .createdAt(point.getCreatedAt())
                .build();
    }
}