package com.untitled.ggobook.service;

import com.untitled.ggobook.domain.Comment;
import com.untitled.ggobook.domain.Reply;
import com.untitled.ggobook.dto.MyActivityDto;
import com.untitled.ggobook.repository.CommentRepository;
import com.untitled.ggobook.repository.ReplyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MyActivityService {

    private final CommentRepository commentRepository;
    private final ReplyRepository replyRepository;

    @Transactional(readOnly = true)
    public List<MyActivityDto> getMyAllCommentsAndReplies(Long id, Pageable pageable) {
        List<Comment> myComments = commentRepository.findByUser_IdAndIsDeletedFalseOrderByCreatedAtDesc(id, pageable).getContent();
        List<Reply> myReplies = replyRepository.findMyReplies(id, pageable).getContent();

        List<MyActivityDto> combinedList = new ArrayList<>();

        myComments.forEach(c -> combinedList.add(MyActivityDto.fromComment(c)));
        myReplies.forEach(r -> combinedList.add(MyActivityDto.fromReply(r)));

        combinedList.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));

        return combinedList;
    }
}