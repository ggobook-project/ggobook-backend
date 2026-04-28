package com.untitled.ggobook.service;

import com.untitled.ggobook.domain.RelayEntry;
import com.untitled.ggobook.domain.RelayNovel;
import com.untitled.ggobook.dto.MyRelayNovelDto;
import com.untitled.ggobook.repository.RelayNovelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MyRelayNovelService {

    private final RelayNovelRepository relayNovelRepository;

    @Transactional(readOnly = true)
    public List<MyRelayNovelDto> getMyRelayNovels(Long userId) {
        // 1. 내 관련 소설 싹 다 가져오기
        List<RelayNovel> novels = relayNovelRepository.findMyRelayNovels(userId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");

        // 2. 프론트엔드 카드 모양(DTO)에 맞게 하나씩 예쁘게 포장하기
        return novels.stream().map(novel -> {
            boolean isStarter = novel.getUserId().equals(userId);
            String role = isStarter ? "시작자" : "참여자";

            // 내 이어쓰기 횟수 계산
            int myEntriesCount = (int) novel.getEntries().stream()
                    .filter(e -> e.getUserId().equals(userId))
                    .count();

            // 총 참여자 수 계산 (방장 + 이어쓰기 한 사람들 싹 다 Set에 넣어서 중복 제거)
            Set<Long> participantsSet = new HashSet<>();
            participantsSet.add(novel.getUserId());
            novel.getEntries().forEach(e -> participantsSet.add(e.getUserId()));
            int participantsCount = participantsSet.size();

            // 참여일 계산 (시작자면 방 만든 날, 참여자면 내가 첫 글 쓴 날)
            LocalDateTime joinDate;
            if (isStarter) {
                joinDate = novel.getCreatedAt();
            } else {
                joinDate = novel.getEntries().stream()
                        .filter(e -> e.getUserId().equals(userId))
                        .map(RelayEntry::getCreatedAt)
                        .min(LocalDateTime::compareTo)
                        .orElse(novel.getCreatedAt()); // 만약 못 찾으면 기본값
            }

            // DTO 완성
            return MyRelayNovelDto.builder()
                    .id(novel.getRelayNovelId())
                    .title(novel.getTitle())
                    .role(role)
                    .entries(myEntriesCount)
                    .participants(participantsCount)
                    .date(joinDate != null ? joinDate.format(formatter) : "")
                    .build();
        }).collect(Collectors.toList());
    }
}