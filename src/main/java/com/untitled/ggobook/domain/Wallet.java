package com.untitled.ggobook.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "wallet")
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long walletId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private Integer balance = 0; // 초기 잔액은 0원

    // 🌟 대기업식 동시성 방어 (Optimistic Lock)
    // 두 명이 동시에 결제하려고 할 때, 이 version이 다르면 에러를 발생시켜 돈 복사를 막습니다.
    @Version
    private Long version;

    // 포인트 내역 (양방향)
    @ToString.Exclude
    @OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL)
    private List<Point> pointHistories = new ArrayList<>();

    // 결제 내역 (양방향)
    @ToString.Exclude
    @OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL)
    private List<Payment> payments = new ArrayList<>();
}