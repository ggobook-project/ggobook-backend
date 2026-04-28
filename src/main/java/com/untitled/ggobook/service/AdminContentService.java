package com.untitled.ggobook.service;

import com.untitled.ggobook.domain.ComicToon;
import com.untitled.ggobook.domain.Content;
import com.untitled.ggobook.domain.Episode;
import com.untitled.ggobook.domain.enums.Status;
import com.untitled.ggobook.dto.ComicToonListDTO;
import com.untitled.ggobook.dto.ContentBasicDTO;
import com.untitled.ggobook.dto.EpisodeDTO;
import com.untitled.ggobook.dto.NovelListDTO;
import com.untitled.ggobook.repository.ContentRepository;
import com.untitled.ggobook.repository.EpisodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminContentService {

    private final ContentRepository contentRepository;
    private final EpisodeRepository episodeRepository;

    @Transactional(readOnly = true)
    public List<?> getContentsByType(String type, String keyword, String day) {
        List<Content> contents;

        // 요일 필터링 (day가 "전체"면 null로 처리하여 쿼리에서 무시하게 함)
        String filterDay = (day == null || "전체".equals(day)) ? null : day;

        if (keyword != null && !keyword.isEmpty()) {
            // 검색어가 있을 때는 기존 복합 검색 로직 사용
            contents = contentRepository.findByTypeAndTitleContainingOrTypeAndAuthor_NicknameContainingOrderByContentIdDesc(
                    type, keyword, type, keyword);

            // 검색 결과 중 요일 필터가 있다면 추가 필터링 (필요시)
            if (filterDay != null) {
                contents = contents.stream()
                        .filter(c -> filterDay.equals(c.getSerialDay()))
                        .collect(Collectors.toList());
            }
        } else {
            // 검색어가 없으면 새로 만든 타입+요일 조회 메서드 사용
            contents = contentRepository.findByTypeAndSerialDay(type, filterDay);
        }

        // DTO 변환 로직 (기존과 동일)
        if ("웹툰".equals(type)) {
            return contents.stream().map(ComicToonListDTO::new).collect(Collectors.toList());
        } else if ("웹소설".equals(type)) {
            return contents.stream().map(NovelListDTO::new).collect(Collectors.toList());
        }
        throw new IllegalArgumentException("지원하지 않는 콘텐츠 타입입니다: " + type);
    }

    /**
     * 🌟 특정 작품의 전체 회차 목록 조회
     * @param contentId 작품 ID
     * @return 회차 DTO 리스트 (회차 번호 순으로 정렬)
     */
    @Transactional(readOnly = true)
    public List<EpisodeDTO> getEpisodeList(Long contentId) {
        // 1. EpisodeRepository를 통해 contentId가 일치하는 회차들을 가져옵니다.
        return episodeRepository.findByContent_ContentIdOrderByEpisodeNumberDesc(contentId).stream()
                .map(EpisodeDTO::new) // 🌟 엔티티를 DTO로 변환
                .collect(Collectors.toList());
    }

    @Transactional
    public ContentBasicDTO getContentBasicInfo(Long contentId) {
        return new ContentBasicDTO(contentRepository.findById(contentId)
                .orElseThrow(() -> new IllegalArgumentException("작품을 찾을 수 없습니다.")));
    }

    // 🌟 회차 상태를 토글(공개<->비공개)하는 메서드
    @Transactional
    public void toggleEpisodeBlindStatus(Long episodeId) {
        Episode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 회차를 찾을 수 없습니다."));

        // 🌟 핵심 로직: 상태가 PUBLISHED면 BLINDED로, BLINDED면 PUBLISHED로 변경
        if (episode.getStatus() == Status.PUBLISHED) {
            episode.setStatus(Status.BLINDED);
        } else if (episode.getStatus() == Status.BLINDED) {
            episode.setStatus(Status.PUBLISHED);
        } else {
            // 이미 반려되거나 임시 저장된 글은 블라인드 처리할 필요가 없으므로 막아둡니다.
            throw new IllegalStateException("공개(PUBLISHED) 또는 블라인드(BLINDED) 상태의 회차만 상태를 변경할 수 있습니다.");
        }
    }

    @Transactional(readOnly = true)
    public EpisodeDTO getEpisodeView(Long episodeId) {
        Episode episode = episodeRepository.findByIdWithDetails(episodeId)
                .orElseThrow(() -> new IllegalArgumentException("회차를 찾을 수 없습니다."));

        EpisodeDTO dto = new EpisodeDTO(episode);

        // 데이터 타입 분기 처리
        if (!episode.getComicToons().isEmpty()) {
            dto.setContentType("WEBTOON");
            dto.setImageUrls(episode.getComicToons().stream()
                    .sorted(Comparator.comparing(ComicToon::getImageOrder)) // 🌟 정렬 중요!
                    .map(ComicToon::getImageUrl).collect(Collectors.toList()));
        } else if (episode.getNovel() != null) {
            dto.setContentType("NOVEL");
            dto.setNovelContent(episode.getNovel().getContentText());
        }
        return dto;
    }
}
