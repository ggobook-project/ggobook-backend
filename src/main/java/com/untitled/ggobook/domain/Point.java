package com.untitled.ggobook.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "point")
public class Point {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pointId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    // CHARGE(충전) / DEDUCT(차감)
    @Column(nullable = false, length = 20)
    private String pointType;

    @Column(nullable = false)
    private Integer amount;

    @Column(nullable = false, length = 200)
    private String description;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
