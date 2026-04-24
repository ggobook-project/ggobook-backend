package com.untitled.ggobook.dto;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Slice;

@Getter
@Builder
public class MyPointResponseDto {
    private Integer currentBalance; // 상단에 크게 띄워줄 현재 잔액
    private Slice<PointHistoryDto> historyList; // 하단에 무한 스크롤로 뿌려줄 내역들
}