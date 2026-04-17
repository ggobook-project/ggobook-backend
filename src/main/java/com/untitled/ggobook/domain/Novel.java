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
}
