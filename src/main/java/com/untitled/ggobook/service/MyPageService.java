package com.untitled.ggobook.service;

import com.untitled.ggobook.domain.Comment;
import com.untitled.ggobook.domain.Content;
import com.untitled.ggobook.domain.Episode;
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
    private final ContentRepository contentRepository; // 조립을 위해 ContentRepo 호출
    private final PasswordEncoder passwordEncoder;
    private final LikeRepository likeRepository;

    private final CommentRepository commentRepository;
    private final ReplyRepository replyRepository;

    private final WalletRepository walletRepository;
    private final PointRepository pointRepository;

    // 1. 내 정보 및 작성 글 목록 조회
    @Transactional(readOnly = true)
    public MyProfileResponse getMyProfile(String loginId) {
        // 1-1. 토큰의 String 아이디로 유저 엔티티를 찾습니다.
        User user = userRepository.findByUserId(loginId)
                .orElseThrow(() -> new RuntimeException("회원 정보를 찾을 수 없습니다."));

        // 1-2. 유저의 Long 고유 번호(id)로 작성 글 목록을 찾아옵니다.
        List<Content> contents = contentRepository.findByAuthorId(user.getId());

        // 1-3. 찾아온 Content 엔티티들을 프론트엔드용 예쁜 DTO로 싹 변환합니다.
        List<MyPostDto> postDtos = contents.stream()
                .map(MyPostDto::from)
                .collect(Collectors.toList());

        // 1-4. 최종 응답 상자에 담아서 반환!
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
    public void updateMyInfo(String loginId, UpdateMyInfoRequest request) {
        User user = userRepository.findByUserId(loginId)
                .orElseThrow(() -> new RuntimeException("회원 정보를 찾을 수 없습니다."));

        // 닉네임 변경 (기존 닉네임과 다를 경우에만 중복 검사)
        if (request.getNickname() != null && !request.getNickname().equals(user.getNickname())) {
            if (userRepository.existsByNickname(request.getNickname())) {
                throw new RuntimeException("이미 사용 중인 닉네임입니다.");
            }
            user.setNickname(request.getNickname());
        }

        // 비밀번호 변경 (입력값이 있을 경우에만 암호화해서 덮어쓰기)
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        // JPA의 더티 체킹(Dirty Checking) 덕분에 save()를 명시하지 않아도 트랜잭션 종료 시 자동 UPDATE 됨
    }
    // 3. 나의 찜 목록 조회 (무한 스크롤용 Slice 반환)
    @Transactional(readOnly = true)
    public Slice<LikedContentDto> getMyLikedContents(String loginId, Pageable pageable) {
        // 1. 토큰의 String 아이디로 유저의 고유번호(Long)를 찾습니다.
        User user = userRepository.findByUserId(loginId)
                .orElseThrow(() -> new RuntimeException("회원 정보를 찾을 수 없습니다."));

        // 2. 해당 유저가 찜한 목록을 Slice로 가져옵니다.
        Slice<Likes> likesSlice = likeRepository.findByUserId(user.getId(), pageable);

        // 3. Likes 엔티티 안에 들어있는 Content를 꺼내서 예쁜 DTO 전단지로 바꿔서 반환합니다!
        return likesSlice.map(like -> LikedContentDto.from(like.getContent()));
    }

    // 4. 내가 작성한 부모 댓글 + 자식 답글 통합 조회 (마이페이지용)
    @Transactional(readOnly = true)
    public List<MyActivityDto> getMyAllCommentsAndReplies(String loginId, Pageable pageable) {
        User user = userRepository.findByUserId(loginId)
                .orElseThrow(() -> new RuntimeException("회원 정보를 찾을 수 없습니다."));

        // 1. 내가 쓴 댓글과 답글을 각각 DB에서 가져옵니다.
        List<Comment> myComments = commentRepository.findMyComments(user.getId(), pageable).getContent();
        List<Reply> myReplies = replyRepository.findMyReplies(user.getId(), pageable).getContent();

        // 2. 둘 다 MyActivityDto라는 동일한 포장지에 예쁘게 담습니다.
        List<MyActivityDto> combinedList = new ArrayList<>();

        myComments.forEach(c -> combinedList.add(MyActivityDto.fromComment(c)));
        myReplies.forEach(r -> combinedList.add(MyActivityDto.fromReply(r)));

        // 3. 댓글과 답글이 섞여 있으니 최신 작성일(createdAt) 기준으로 통합 정렬해서 프론트로 던져줍니다!
        combinedList.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));

        return combinedList;
    }

    // 5. 내 지갑 잔액 및 포인트 내역 통합 조회
    @Transactional(readOnly = true)
    public MyPointResponseDto getMyPoints(String loginId, Pageable pageable) {
        User user = userRepository.findByUserId(loginId)
                .orElseThrow(() -> new RuntimeException("회원 정보를 찾을 수 없습니다."));

        // 1. 지갑에서 현재 잔고만 쏙 가져옵니다. (지갑이 없으면 잔액 0원으로 처리)
        Integer currentBalance = walletRepository.findByUserId(user.getId())
                .map(Wallet::getBalance)
                .orElse(0);

        // 2. 장부(Point)에서 내역을 최신순으로 긁어와서 DTO로 싹 변환합니다.
        Slice<PointHistoryDto> historySlice = pointRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable)
                .map(PointHistoryDto::from);

        // 3. 잔액과 내역을 하나의 박스에 예쁘게 담아서 반환합니다!
        return MyPointResponseDto.builder()
                .currentBalance(currentBalance)
                .historyList(historySlice)
                .build();
    }

}