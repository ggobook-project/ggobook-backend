package com.untitled.ggobook.service;

import com.untitled.ggobook.domain.Comment;
import com.untitled.ggobook.domain.Content;
import com.untitled.ggobook.domain.Likes;
import com.untitled.ggobook.domain.Reply;
import com.untitled.ggobook.domain.User;
import com.untitled.ggobook.domain.Wallet;

import com.untitled.ggobook.dto.LikedContentDto;
import com.untitled.ggobook.dto.MyPostDto;
import com.untitled.ggobook.dto.MyProfileResponse;
import com.untitled.ggobook.dto.UpdateMyInfoRequest;

import com.untitled.ggobook.dto.MyActivityDto;
import com.untitled.ggobook.dto.MyPointResponseDto;
import com.untitled.ggobook.dto.PointHistoryDto;

import com.untitled.ggobook.repository.CommentRepository;
import com.untitled.ggobook.repository.ContentRepository;
import com.untitled.ggobook.repository.LikeRepository;
import com.untitled.ggobook.repository.ReplyRepository;
import com.untitled.ggobook.repository.UserRepository;
import com.untitled.ggobook.repository.WalletRepository;
import com.untitled.ggobook.repository.PointRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MyPageService {

    private final UserRepository userRepository;
    private final ContentRepository contentRepository;
    private final PasswordEncoder passwordEncoder;
    private final LikeRepository likeRepository;

    private final CommentRepository commentRepository;
    private final ReplyRepository replyRepository;

    private final WalletRepository walletRepository;
    private final PointRepository pointRepository;

    // 1. 내 정보 및 작성 글 목록 조회
    @Transactional(readOnly = true)
    public MyProfileResponse getMyProfile(Long id) { // 🌟 Long id 사용

        // 🌟 String 검색(findByUserId) 대신, DB에서 가장 빠른 PK 검색(findById) 사용!
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("회원 정보를 찾을 수 없습니다."));

        List<Content> contents = contentRepository.findByAuthorId(id);

        List<MyPostDto> postDtos = contents.stream()
                .map(MyPostDto::from)
                .collect(Collectors.toList());

        return MyProfileResponse.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .myPosts(postDtos)
                .build();
    }

    // 2. 내 정보 수정 (닉네임, 비밀번호)
    @Transactional
    public void updateMyInfo(Long id, UpdateMyInfoRequest request) { // 🌟 Long id 사용

        // 🌟 PK로 초고속 조회
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("회원 정보를 찾을 수 없습니다."));

        if (request.getNickname() != null && !request.getNickname().equals(user.getNickname())) {
            if (userRepository.existsByNickname(request.getNickname())) {
                throw new RuntimeException("이미 사용 중인 닉네임입니다.");
            }
            user.setNickname(request.getNickname());
        }

        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
    }

    // 3. 나의 찜 목록 조회
    @Transactional(readOnly = true)
    public Slice<LikedContentDto> getMyLikedContents(Long id, Pageable pageable) { // 🌟 Long id 사용

        // 🌟 팩트: 유저 엔티티(User)가 필요 없으므로 userRepository 조회 완전히 삭제! (쿼리 절약)
        Slice<Likes> likesSlice = likeRepository.findByUserId(id, pageable);

        return likesSlice.map(like -> LikedContentDto.from(like.getContent()));
    }

    // 4. 내가 작성한 부모 댓글 + 자식 답글 통합 조회
    @Transactional(readOnly = true)
    public List<MyActivityDto> getMyAllCommentsAndReplies(Long id, Pageable pageable) { // 🌟 Long id 사용

        // 🌟 팩트: 역시 userRepository 조회 삭제! id 들고 각 창고로 바로 직행합니다.
        List<Comment> myComments = commentRepository.findMyComments(id, pageable).getContent();
        List<Reply> myReplies = replyRepository.findMyReplies(id, pageable).getContent();

        List<MyActivityDto> combinedList = new ArrayList<>();

        myComments.forEach(c -> combinedList.add(MyActivityDto.fromComment(c)));
        myReplies.forEach(r -> combinedList.add(MyActivityDto.fromReply(r)));

        combinedList.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));

        return combinedList;
    }

    // 5. 내 지갑 잔액 및 포인트 내역 통합 조회
    @Transactional(readOnly = true)
    public MyPointResponseDto getMyPoints(Long id, Pageable pageable) { // 🌟 Long id 사용

        // 🌟 팩트: userRepository 조회 삭제! id 들고 지갑(Wallet)과 장부(Point)로 직행.
        Integer currentBalance = walletRepository.findByUserId(id)
                .map(Wallet::getBalance)
                .orElse(0);

        Slice<PointHistoryDto> historySlice = pointRepository.findByUserIdOrderByCreatedAtDesc(id, pageable)
                .map(PointHistoryDto::from);

        return MyPointResponseDto.builder()
                .currentBalance(currentBalance)
                .historyList(historySlice)
                .build();
    }
}