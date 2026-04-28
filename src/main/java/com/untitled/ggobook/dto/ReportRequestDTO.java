package com.untitled.ggobook.dto;

import com.untitled.ggobook.domain.enums.ReportReason;
import com.untitled.ggobook.domain.enums.TargetType;
import lombok.Data;

@Data
public class ReportRequestDTO {
    private TargetType targetType;   // 무엇을 (Enum)
    private Long targetId;           // 어떤 게시물을
    private Long targetParentId;     // 소설 ID를 담아서 보냄
    private ReportReason reportReason;     // 왜 (Enum)
    private Long reportedUserId;     // 누구를 (신고 대상 유저 ID)
}