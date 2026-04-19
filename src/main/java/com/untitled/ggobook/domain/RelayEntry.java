package com.untitled.ggobook.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "relay_entry")
public class RelayEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long entryId;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "relay_novel_id", nullable = false)
    @JsonIgnore
    private RelayNovel relayNovel;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String entryText;

    @Column(nullable = false)
    private Integer entryOrder;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}