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
import org.springframework.data.domain.Page; // 🌟 추가
import org.springframework.data.domain.Pageable; // 🌟 추가
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

    // 🌟 수정: Pageable을 파라미터로 받고 Page<?>를 반환하도록 변경
    @Transactional(readOnly = true)
    public Page<?> getContentsByType(String type, String keyword, String day, Pageable pageable) {

        // 검색어와 요일이 빈 값이면 null로 변환하여 동적 쿼리가 무시하도록 처리
        String filterDay = (day == null || "전체".equals(day) || day.trim().isEmpty()) ? null : day;
        String filterKeyword = (keyword == null || keyword.trim().isEmpty()) ? null : keyword;

        // 🌟 Repository의 페이징 전용 쿼리 호출 (DB에서 필터링 완료)
        Page<Content> contents = contentRepository.findAdminContentsWithPaging(type, filterKeyword, filterDay, pageable);

        // 🌟 Page.map()을 사용하면 내부의 데이터들만 DTO로 깔끔하게 변환되면서 Page 구조(총 페이지 수 등)는 유지됩니다.
        if ("웹툰".equals(type) || "WEBTOON".equals(type)) {
            return contents.map(ComicToonListDTO::new);
        } else if ("웹소설".equals(type) || "NOVEL".equals(type)) {
            return contents.map(NovelListDTO::new);
        }

        throw new IllegalArgumentException("지원하지 않는 콘텐츠 타입입니다: " + type);
    }

    @Transactional(readOnly = true)
    public List<EpisodeDTO> getEpisodeList(Long contentId) {
        return episodeRepository.findByContent_ContentIdOrderByEpisodeNumberDesc(contentId).stream()
                .map(EpisodeDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public ContentBasicDTO getContentBasicInfo(Long contentId) {
        return new ContentBasicDTO(contentRepository.findById(contentId)
                .orElseThrow(() -> new IllegalArgumentException("작품을 찾을 수 없습니다.")));
    }

    @Transactional
    public void toggleEpisodeBlindStatus(Long episodeId) {
        Episode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 회차를 찾을 수 없습니다."));
        if (episode.getStatus() == Status.PUBLISHED || episode.getStatus() == Status.APPROVED) {
            episode.setStatus(Status.BLINDED);
        } else if (episode.getStatus() == Status.BLINDED) {
            episode.setStatus(Status.APPROVED);
        } else {
            throw new IllegalStateException("공개 중이거나 블라인드 상태인 회차만 변경할 수 있습니다. (현재 상태: " + episode.getStatus() + ")");
        }
    }

    @Transactional(readOnly = true)
    public EpisodeDTO getEpisodeView(Long episodeId) {
        Episode episode = episodeRepository.findByIdWithDetails(episodeId)
                .orElseThrow(() -> new IllegalArgumentException("회차를 찾을 수 없습니다."));

        EpisodeDTO dto = new EpisodeDTO(episode);

        if (!episode.getComicToons().isEmpty()) {
            dto.setContentType("WEBTOON");
            dto.setImageUrls(episode.getComicToons().stream()
                    .sorted(Comparator.comparing(ComicToon::getImageOrder))
                    .map(ComicToon::getImageUrl).collect(Collectors.toList()));
        } else if (episode.getNovel() != null) {
            dto.setContentType("NOVEL");
            dto.setNovelContent(episode.getNovel().getContentText());
        }
        return dto;
    }

    @Transactional
    public void blindEntireContent(Long contentId) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new IllegalArgumentException("작품을 찾을 수 없습니다."));
        content.setStatus(Status.BLINDED);
        // 딸려있는 회차도 전부 블라인드 처리
        for (Episode episode : content.getEpisodes()) {
            episode.setStatus(Status.BLINDED);
        }
    }
}