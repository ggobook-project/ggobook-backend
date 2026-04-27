package com.untitled.ggobook.service;

import com.untitled.ggobook.domain.*;
import com.untitled.ggobook.dto.EntryDTO;
import com.untitled.ggobook.dto.RelayNovelCreateRequestDTO;
import com.untitled.ggobook.dto.RelayNovelDTO;
import com.untitled.ggobook.dto.RelayNovelListDTO;
import com.untitled.ggobook.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RelayNovelService {

    private final UserRepository userRepository;
    private final RelayNovelRepository relayNovelRepository;
    private final RelayTopicRepository relayTopicRepository; // 🌟 추가
    private final RelayEntryRepository relayEntryRepository; // 🌟 추가
    private final AdminRelayTopicRepository adminRelayTopicRepository;

    // 1. 목록 조회 (정렬 분기 처리)
    @Transactional(readOnly = true)
    public Page<RelayNovelListDTO> getRelayNovels(String sortType, Pageable pageable) {
        Page<RelayNovel> novelPage = ("popular".equals(sortType))
                ? relayNovelRepository.findAllOrderByEntryCountDescAndTitleAsc(pageable)
                : relayNovelRepository.findAllByOrderByCreatedAtDesc(pageable);

        // 1. 소설에서 모든 유저 ID를 추출 (중복 제거)
        Set<Long> userIds = novelPage.getContent().stream()
                .map(RelayNovel::getUserId)
                .collect(Collectors.toSet());

        // 2. 닉네임들을 한 번에 조회 (Map<userId, nickname> 형태)
        Map<Long, String> nicknameMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, User::getNickname));

        // 3. DTO 변환 시 맵에서 꺼내서 사용
        return novelPage.map(novel -> {
            String nickname = nicknameMap.getOrDefault(novel.getUserId(), "알 수 없음");
            return new RelayNovelListDTO(novel, nickname);
        });
    }

    // 2. 상세 조회
    @Transactional(readOnly = true)
    public RelayNovelDTO getRelayNovelDetail(Long novelId) {
        // 1. 이미 entries까지 한 번에 JOIN FETCH로 가져오는 레포지토리 메서드 사용
        RelayNovel novel = relayNovelRepository.findByIdWithEntries(novelId)
                .orElseThrow(() -> new IllegalArgumentException("해당 소설을 찾을 수 없습니다."));

        // 2. 소설 작성자(starter)와 회차 작성자들의 ID 목록을 수집
        Set<Long> userIds = novel.getEntries().stream()
                .map(RelayEntry::getUserId)
                .collect(Collectors.toSet());
        userIds.add(novel.getUserId()); // 소설 작성자도 추가

        // 3. 닉네임 맵 한 번에 조회 (쿼리 1번)
        Map<Long, String> nicknameMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, User::getNickname));

        // 4. 소설 작성자 닉네임 매핑
        String starterNickname = nicknameMap.getOrDefault(novel.getUserId(), "알 수 없음");

        // 5. DTO 생성 (닉네임 전달)
        RelayNovelDTO dto = new RelayNovelDTO(novel, starterNickname);

        // 6. 🌟 EntryDTO 닉네임 매핑 로직 추가
        dto.setEntries(novel.getEntries().stream()
                .map(entry -> {
                    EntryDTO eDto = new EntryDTO(entry);
                    eDto.setNickname(nicknameMap.getOrDefault(entry.getUserId(), "알 수 없음"));
                    return eDto;
                })
                .collect(Collectors.toList()));

        return dto;
    }

    @Transactional
    public Long createRelayNovel(Long userId, RelayNovelCreateRequestDTO dto) {
        // 1) 주제(RelayTopic) 결정 로직
        RelayTopic topic;
        if (dto.getAdminTopicId() != null) {
            // 관리자 주제 사용
            AdminRelayTopic adminTopic = adminRelayTopicRepository.findById(dto.getAdminTopicId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 관리자 주제입니다."));

            topic = new RelayTopic();
            topic.setUserId(userId);
            topic.setAdminTopic(adminTopic);
            topic.setTitle(adminTopic.getTitle());
            topic.setDescription(adminTopic.getDescription());
        } else {
            // 자유 주제 사용
            topic = new RelayTopic();
            topic.setUserId(userId);
            topic.setTitle(dto.getCustomTitle());
            topic.setDescription(dto.getCustomDescription());
        }
        relayTopicRepository.save(topic);

        // 2) 소설(RelayNovel) 생성
        RelayNovel novel = new RelayNovel();
        novel.setUserId(userId);
        novel.setRelayTopic(topic);
        novel.setTitle(dto.getNovelTitle());
        novel.setDescription(dto.getCustomDescription()); // 소설 상세 설명
        relayNovelRepository.save(novel);

        // 3) 1회차(RelayEntry) 생성
        RelayEntry firstEntry = new RelayEntry(novel, userId, dto.getFirstEntryText(), 1);
        relayEntryRepository.save(firstEntry);

        return novel.getRelayNovelId();
    }
}
