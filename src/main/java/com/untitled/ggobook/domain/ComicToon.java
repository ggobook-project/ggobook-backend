package com.untitled.ggobook.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

@Entity
@Data
public class ComicToon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long imageId;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "episode_id")
    @JsonIgnore // ✅ Jackson이 에피소드로 다시 돌아가는 것을 막아 무한 루프 방지
    private Episode episode;

    private String imageUrl;
    private Integer imageOrder;
}