package com.untitled.ggobook.dto;

import com.untitled.ggobook.domain.RelayNovel;
import com.untitled.ggobook.domain.RelayEntry;
import com.untitled.ggobook.domain.enums.Status;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RelayNovelListDTO {
    private Long relayNovelId;
    private String title;
    private String description;
    private String starterNickname;
    private Long starterId; // 🌟 1. 작성자 ID를 담을 필드 추가
    private Status status;
    private long uniqueParticipantCount;
    private int entryCount;
    private LocalDateTime createdAt;

    public RelayNovelListDTO(RelayNovel novel, String nickname) {
        this.relayNovelId = novel.getRelayNovelId();
        this.title = novel.getTitle();
        this.description = (novel.getDescription() != null && !novel.getDescription().isEmpty())
                ? novel.getDescription()
                : "아직 소개글이 없습니다. 첫 번째 이야기를 시작해 보세요!";

        this.starterNickname = nickname;

        // 🌟 [핵심 수정] 엔티티의 userId를 DTO의 starterId에 매핑합니다.
        this.starterId = novel.getUserId();

        this.createdAt = novel.getCreatedAt();
        this.status = novel.getStatus();

        if (novel.getEntries() != null) {
            this.uniqueParticipantCount = novel.getEntries().stream()
                    .map(RelayEntry::getUserId)
                    .distinct()
                    .count();
            this.entryCount = novel.getEntries().size();
        } else {
            this.uniqueParticipantCount = 0;
            this.entryCount = 0;
        }
    }
}
