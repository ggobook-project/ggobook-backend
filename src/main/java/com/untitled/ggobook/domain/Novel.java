package com.untitled.ggobook.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

@Entity
@Data
public class Novel {
    @Id
    private Long episodeId;

    @ToString.Exclude
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "episode_id")
    @JsonIgnore // ✅ Jackson이 에피소드로 다시 돌아가는 것을 막아 무한 루프 방지
    private Episode episode;

    @Column(columnDefinition = "LONGTEXT")
    private String contentText;

    private String ttsFileUrl;

    public static Novel createDraft(Novel existing, Episode draftEpisode) {
        Novel draft = new Novel(); // 클래스 내부이므로 안전하게 생성 가능!
        draft.setContentText(existing.getContentText());

        if (existing.getTtsFileUrl() != null) {
            draft.setTtsFileUrl(existing.getTtsFileUrl());
        }

        draft.setEpisode(draftEpisode);
        return draft;
    }
}