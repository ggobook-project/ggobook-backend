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
        this.createdAt = novel.getCreatedAt();
        this.status = novel.getStatus(); // 🌟 추가: 엔티티에서 상태값 가져오기

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