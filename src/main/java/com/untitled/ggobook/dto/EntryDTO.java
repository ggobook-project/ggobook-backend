package com.untitled.ggobook.dto;

import com.untitled.ggobook.domain.RelayEntry;
import com.untitled.ggobook.domain.enums.Status; // 🌟 import 추가
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EntryDTO {
    private Long entryId;
    private Long userId;
    private String nickname;

    private String profileImageUrl;
    private int entryOrder;
    private String entryText;
    private LocalDateTime createdAt;
    private Status status;
    private String adminMessage;

    public EntryDTO(RelayEntry entry) {
        this.entryId = entry.getEntryId();
        this.userId = entry.getUserId();
        this.entryOrder = entry.getEntryOrder();
        this.entryText = entry.getEntryText();
        this.createdAt = entry.getCreatedAt();
        this.status = entry.getStatus();
        this.adminMessage = entry.getAdminMessage();
    }

    public void setNickname(String nickname) { this.nickname = nickname; }
}