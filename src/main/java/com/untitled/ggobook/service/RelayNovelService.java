package com.untitled.ggobook.service;

import com.untitled.ggobook.component.RedisLockManager;
import com.untitled.ggobook.domain.*;
import com.untitled.ggobook.domain.enums.Status;
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
    private final RedisLockManager redisLockManager;

    // 1. 목록 조회 (정렬 분기 처리)
    @Transactional(readOnly = true)
    public Page<RelayNovelListDTO> getRelayNovels(String sortType, Pageable pageable) {
        // 🌟 Repository가 변경되었으므로 Status.PRIVATE을 함께 넘겨줍니다.
        Page<RelayNovel> novelPage = ("popular".equals(sortType))
                ? relayNovelRepository.findAllOrderByEntryCountDescAndTitleAsc(Status.PRIVATE, pageable)
                : relayNovelRepository.findByStatusNotOrderByCreatedAtDesc(Status.PRIVATE, pageable);

        // 1. 소설에서 모든 유저 ID를 추출 (중복 제거)
        Set<Long> userIds = novelPage.getContent().stream()
                .map(RelayNovel::getUserId)
                .collect(Collectors.toSet());

        // 2. 닉네임들을 한 번에 조회
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
        // 1. 이미 entries까지 한 번에 JOIN FETCH로 가져오는 레포지토리 메서드 사용 (기존 완벽 유지)
        RelayNovel novel = relayNovelRepository.findByIdWithEntries(novelId)
                .orElseThrow(() -> new IllegalArgumentException("해당 소설을 찾을 수 없습니다."));

        // 2. 소설 작성자(starter)와 회차 작성자들의 ID 목록을 수집 (기존 완벽 유지)
        Set<Long> userIds = novel.getEntries().stream()
                .map(RelayEntry::getUserId)
                .collect(Collectors.toSet());
        userIds.add(novel.getUserId()); // 소설 작성자도 추가

        // 3. 닉네임 맵 한 번에 조회 (🌟 업그레이드: String 닉네임만 담지 말고 User 객체를 통째로 담기!)
        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        // 4. 소설 작성자 닉네임 매핑 (🌟 업그레이드: User에서 닉네임을 꺼내면서 탈퇴 여부까지 체크!)
        User starter = userMap.get(novel.getUserId());
        String starterNickname = (starter != null && starter.getStatus() != com.untitled.ggobook.domain.enums.UserStatus.WITHDRAWN)
                ? (starter.getNickname() != null ? starter.getNickname() : "알 수 없음")
                : "탈퇴한 회원";

        // 5. DTO 생성 (닉네임 전달) (기존 완벽 유지)
        RelayNovelDTO dto = new RelayNovelDTO(novel, starterNickname);

        // 6. EntryDTO 닉네임 매핑 로직 추가 (🌟 업그레이드: 닉네임뿐만 아니라 사진도 넣고, 탈퇴자면 가리기!)
        dto.setEntries(novel.getEntries().stream()
                .map(entry -> {
                    EntryDTO eDto = new EntryDTO(entry);
                    User author = userMap.get(entry.getUserId());

                    if (author != null && author.getStatus() != com.untitled.ggobook.domain.enums.UserStatus.WITHDRAWN) {
                        eDto.setNickname(author.getNickname() != null ? author.getNickname() : "알 수 없음");
                        eDto.setProfileImageUrl(author.getProfileImageUrl()); // 📸 사진 챙기기
                    } else {
                        eDto.setNickname("탈퇴한 회원"); // 🚫 탈퇴자 익명 처리
                        eDto.setProfileImageUrl(null);   // 🚫 탈퇴자 사진 가리기
                    }
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

    // 1. [이어쓰기] 버튼 클릭 시
    public void startWriting(Long novelId, Long userId) {
        Boolean isLocked = redisLockManager.lock(novelId, userId);

        if (!isLocked) {
            // 🚨 초보자 주의 포인트: 여기서 예외를 던지면 컨트롤러를 거쳐 프론트엔드로 409 같은 에러 코드가 갑니다.
            throw new IllegalStateException("현재 다른 유저가 작성 중입니다. 잠시 후 다시 시도해 주세요.");
        }
        // 락 획득 성공! 프론트엔드는 이제 에디터 화면으로 넘어갑니다.
    }

    // 2. [작성 완료(등록)] 버튼 클릭 시
    @Transactional
    public void submitEpisode(Long novelId, Long userId, String content) {
        try {
            // 1. 소설 확인
            RelayNovel novel = relayNovelRepository.findById(novelId)
                    .orElseThrow(() -> new IllegalArgumentException("소설을 찾을 수 없습니다."));

            // 2. 마지막 순번 계산 (Repository 메서드 활용)
            Integer lastOrder = relayEntryRepository.findTopByRelayNovel_RelayNovelIdOrderByEntryOrderDesc(novelId)
                    .map(RelayEntry::getEntryOrder)
                    .orElse(0);

            // 3. 새 엔트리 생성 및 저장
            RelayEntry newEntry = RelayEntry.builder()
                    .relayNovel(novel)
                    .userId(userId)
                    .entryText(content)
                    .entryOrder(lastOrder + 1)
                    .build();

            relayEntryRepository.save(newEntry);

        } catch (Exception e) {
            // 로그를 남겨주면 디버깅이 훨씬 쉽습니다!
            System.err.println("에피소드 저장 실패: " + e.getMessage());
            throw e;
        } finally {
            // 4. 어떤 경우에도 락은 무조건 해제!
            redisLockManager.unlock(novelId);
        }
    }

    // 3. [작성 취소] 버튼 클릭 또는 뒤로 가기 시
    public void cancelWriting(Long novelId) {
        redisLockManager.unlock(novelId);
    }
}
