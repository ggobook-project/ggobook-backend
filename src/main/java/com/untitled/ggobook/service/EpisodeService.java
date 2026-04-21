package com.untitled.ggobook.service;

import com.untitled.ggobook.domain.Content;
import com.untitled.ggobook.domain.Episode;
import com.untitled.ggobook.repository.EpisodeRepository;
import com.untitled.ggobook.util.FileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

// 회차 서비스
@Service
@Slf4j
@RequiredArgsConstructor
public class EpisodeService {
    private final EpisodeRepository episodeRepository;
    private final FileUtil fileUtil;

    @Transactional(readOnly = true)
    public Episode getEpisodeDetail(Long episodeId) {
        // ✅ [수정] 기본 findById 대신 Fetch Join이 적용된 쿼리를 사용합니다.
        return episodeRepository.findByIdWithDetails(episodeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 회차를 찾을 수 없습니다."));
    }

    public Slice<Episode> getEpisodeList(Long contentId, Pageable pageable, String currentNeedStatus) {
        return episodeRepository.findEpisodeListByContentId(contentId, pageable, currentNeedStatus);
    }

    @Transactional
    public void registerEpisode(Content content, Episode episode, MultipartFile multipartFile) {
        if(!multipartFile.isEmpty()){
            episode.setThumbnailUrl(fileUtil.uploadToS3(multipartFile));
        }
        episode.setContent(content);
        episodeRepository.save(episode);
    }

    @Transactional
    public void updateEpisode(Episode episode, MultipartFile multipartFile){
        if(episodeRepository.existsById(episode.getEpisodeId())){
            if(!multipartFile.isEmpty()){
                fileUtil.deleteFromS3(episode.getThumbnailUrl());
                episode.setThumbnailUrl(fileUtil.uploadToS3(multipartFile));
            }
            episodeRepository.save(episode);
        }
    }

    @Transactional
    public void deleteEpisode(Long episodeId) {
        Episode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 회차가 없습니다."));

        if (episode.getThumbnailUrl() != null) {
            fileUtil.deleteFromS3(episode.getThumbnailUrl());
        }
        episodeRepository.delete(episode);

    }
}
