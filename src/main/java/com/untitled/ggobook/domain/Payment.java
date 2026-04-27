package com.untitled.ggobook.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "payment")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 포트원 고유 결제 번호
    @Column(unique = true, length = 100)
    private String impUid;

    // 주문 고유 번호
    @Column(unique = true, length = 100)
    private String merchantUid;

    // 결제 수단 (CARD / KAKAO / TOSS)
    @Column(nullable = false, length = 20)
    private String paymentMethod;

    @Column(nullable = false)
    private Integer amount;

    @Column(nullable = false)
    private Integer pointAmount;

    // 결제 상태 (PENDING / COMPLETE / FAILED / CANCEL)
    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    @JsonIgnore
    private Wallet wallet;
}
