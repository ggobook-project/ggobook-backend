package com.untitled.ggobook.service;

import com.untitled.ggobook.domain.Comment;
import com.untitled.ggobook.domain.Episode;
import com.untitled.ggobook.dto.CommentRequestDto;
import com.untitled.ggobook.dto.CommentResponseDto;
import com.untitled.ggobook.repository.CommentRepository;
import com.untitled.ggobook.repository.EpisodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final EpisodeRepository episodeRepository;


    // 1. 웹툰 하단 댓글 목록 조회
    @Transactional(readOnly = true)
    public Slice<CommentResponseDto> getEpisodeComments(Long episodeId, Pageable pageable) {
        return commentRepository.findCommentsWithRepliesByEpisodeId(episodeId, pageable)
                .map(CommentResponseDto::from); // 프론트엔드용 트리 구조 DTO로 싹 변환!
    }

    // 2. 새 댓글 작성
    @Transactional
    public void createComment(Long id, Long episodeId, CommentRequestDto requestDto) {

        //  바로 회차 정보만 찾습니다.
        Episode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new IllegalArgumentException("회차 없음"));

        Comment comment = new Comment();
        comment.getUser().setId(id); // 🌟 컨트롤러에서 넘어온 PK(id)를 다이렉트로 꽂아 넣습니다.
        comment.setContent(episode.getContent());
        comment.setEpisode(episode);
        comment.setCommentText(requestDto.getCommentText());
        comment.setIsSpoiler(requestDto.getIsSpoiler() != null ? requestDto.getIsSpoiler() : false);
        comment.setIsDeleted(false); // 초기화

        commentRepository.save(comment);
    }

    // 3. 댓글 삭제 (소프트 삭제 적용)
    @Transactional
    public void deleteComment(Long id, Long commentId) { // 🌟 Long id 사용

        // 댓글만 바로 찾습니다.
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글 없음"));

        // 컨트롤러에서 넘어온 PK(id)와 댓글 주인의 PK를 직접 비교합니다.
        if (!comment.getUser().getId().equals(id)) {
            throw new IllegalArgumentException("본인의 댓글만 삭제할 수 있습니다.");
        }

        // 마법의 삭제 로직: 답글이 없으면 DB에서 아예 날려버리고, 답글이 있으면 간판만 '삭제됨'으로 변경!
        if (comment.getReplies().isEmpty()) {
            commentRepository.delete(comment);
        } else {
            comment.setIsDeleted(true);
            // 텍스트를 아예 덮어씌워서 DB 털려도 내용 안 보이게 하는 옵션
            comment.setCommentText("삭제된 댓글입니다.");
        }
    }
}