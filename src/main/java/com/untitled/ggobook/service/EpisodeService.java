package com.untitled.ggobook.service;

import com.untitled.ggobook.domain.ComicToon;
import com.untitled.ggobook.domain.Content;
import com.untitled.ggobook.domain.Episode;
import com.untitled.ggobook.domain.Novel;
import com.untitled.ggobook.domain.enums.Status;
import com.untitled.ggobook.repository.ComicToonRepository;
import com.untitled.ggobook.repository.EpisodeRepository;
import com.untitled.ggobook.repository.NovelRepository;
import com.untitled.ggobook.util.FileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

// 회차 서비스
@Service
@Slf4j
@RequiredArgsConstructor
public class EpisodeService {
    private final EpisodeRepository episodeRepository;
    private final ComicToonRepository comicToonRepository;
    private final NovelRepository novelRepository;
    private final FileUtil fileUtil;

    @Transactional(readOnly = true)
    public Episode getEpisodeDetail(Long episodeId) {
        return episodeRepository.findByIdWithDetails(episodeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 회차를 찾을 수 없습니다."));
    }

    public Slice<Episode> getEpisodeList(Long contentId, Pageable pageable, String currentNeedStatus) {
        return episodeRepository.findEpisodeListByContentId(contentId, pageable, currentNeedStatus);
    }

    @Transactional
    public void registerEpisode(Content content, Episode episode, Novel novel, MultipartFile thumbFile, List<MultipartFile> episodeFiles) {
        if (thumbFile != null && !thumbFile.isEmpty()) {
            episode.setThumbnailUrl(fileUtil.uploadToS3(thumbFile));
        }

        episode.setContent(content);
        Episode savedEpisode = episodeRepository.save(episode);

        if(novel != null){
            novel.setEpisode(savedEpisode);
            novelRepository.save(novel);
        }else{
            if (episodeFiles != null && !episodeFiles.isEmpty()) {
                for (int com = 0; com < episodeFiles.size(); com++) {
                    ComicToon comicToon = new ComicToon();
                    comicToon.setEpisode(savedEpisode);
                    comicToon.setImageUrl(fileUtil.uploadToS3(episodeFiles.get(com)));
                    comicToon.setImageOrder(com + 1);
                    comicToonRepository.save(comicToon);
                }
            }
        }
    }

    @Transactional
    public void updateEpisode(Episode episode, Novel novel, MultipartFile thumbFile, List<MultipartFile> episodeFiles) {
        Episode existing = episodeRepository.findById(episode.getEpisodeId())
                .orElseThrow(() -> new IllegalArgumentException("회차 없음"));

        if (thumbFile != null && !thumbFile.isEmpty()) {
            fileUtil.deleteFromS3(existing.getThumbnailUrl());
            episode.setThumbnailUrl(fileUtil.uploadToS3(thumbFile));
        }

        Episode savedEpisode = episodeRepository.save(episode);

        if (novel != null) {
            novel.setEpisodeId(savedEpisode.getEpisodeId());
            novel.setEpisode(savedEpisode);
            novelRepository.save(novel);
        } else if (episodeFiles != null && !episodeFiles.isEmpty()) {
            List<ComicToon> oldComicToons = comicToonRepository.findByEpisode(savedEpisode);
            for (ComicToon old : oldComicToons) {
                fileUtil.deleteFromS3(old.getImageUrl());
            }
            comicToonRepository.deleteAll(oldComicToons);

            for (int i = 0; i < episodeFiles.size(); i++) {
                ComicToon comicToon = new ComicToon();
                comicToon.setEpisode(savedEpisode);
                comicToon.setImageUrl(fileUtil.uploadToS3(episodeFiles.get(i)));
                comicToon.setImageOrder(i + 1);
                comicToonRepository.save(comicToon);
            }
        }
    }

    @Transactional
    public void deleteEpisode(Long episodeId) {
        Episode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 회차가 없습니다."));

        if (episode.getThumbnailUrl() != null) {
            fileUtil.deleteFromS3(episode.getThumbnailUrl());
        }

        if (episode.getNovel() != null) {
            novelRepository.delete(episode.getNovel());
        }

        List<ComicToon> comicToons = comicToonRepository.findByEpisode(episode);
        for (ComicToon comicToon : comicToons) {
            fileUtil.deleteFromS3(comicToon.getImageUrl());
        }
        comicToonRepository.deleteAll(comicToons);

        episodeRepository.delete(episode);
    }
}
