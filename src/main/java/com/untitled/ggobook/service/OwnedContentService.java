package com.untitled.ggobook.service;

import com.untitled.ggobook.domain.Content;
import com.untitled.ggobook.domain.OwnedContent;
import com.untitled.ggobook.dto.OwnedEpisodeDto;
import com.untitled.ggobook.repository.OwnedContentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class OwnedContentService {

    private final OwnedContentRepository ownedContentRepository;

    public List<Content> getOwnedContents(Long userId) {
        return ownedContentRepository.findOwnedContentsByUserId(userId);
    }

    public List<OwnedEpisodeDto> getOwnedEpisodesByContent(Long userId, Long contentId) {
        return ownedContentRepository.findOwnedEpisodesByUserIdAndContentId(userId, contentId)
                .stream()
                .map(OwnedEpisodeDto::from)
                .toList();
    }

    public void deleteOwnedContent(Long userId, Long contentId, Long episodeId) {
        OwnedContent ownedContent = ownedContentRepository.findByUserIdAndContentContentIdAndEpisodeEpisodeId(userId, contentId, episodeId);
        if (ownedContent == null) {
            throw new IllegalArgumentException("소장 작품이 아닙니다.");
        }
        ownedContentRepository.delete(ownedContent);
    }
}
