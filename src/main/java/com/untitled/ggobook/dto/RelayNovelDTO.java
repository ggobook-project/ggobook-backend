package com.untitled.ggobook.dto;

import com.untitled.ggobook.domain.RelayNovel;
import lombok.Data;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class RelayNovelDTO {
    private Long relayNovelId;
    private String title;
    private String starterNickname;
    private int participantCount; // 🌟 기존 필드 (DB 저장용)
    private long uniqueParticipantCount; // 🌟 새로 추가: 중복 제거된 참여자 수
    private List<EntryDTO> entries;

    public RelayNovelDTO(RelayNovel novel, String nickname) {
        this.relayNovelId = novel.getRelayNovelId();
        this.title = novel.getTitle();
        this.starterNickname = nickname;

        // 🌟 핵심 로직: 서비스나 DTO 내부에서 중복 제거 계산
        this.uniqueParticipantCount = novel.getEntries().stream()
                .map(entry -> entry.getUserId()) // 작성자 ID 추출
                .distinct()                    // 중복 제거
                .count();                      // 개수 카운트

        this.entries = novel.getEntries().stream().map(EntryDTO::new).collect(Collectors.toList());
    }
}