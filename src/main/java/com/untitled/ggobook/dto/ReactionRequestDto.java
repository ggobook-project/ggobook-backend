package com.untitled.ggobook.dto;

import com.untitled.ggobook.domain.enums.ReactionType;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReactionRequestDto {
    private ReactionType reactionType;
}