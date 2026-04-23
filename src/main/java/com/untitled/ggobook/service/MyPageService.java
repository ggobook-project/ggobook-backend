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
    public void updateMyInfo(Long id, UpdateMyInfoRequest request) {

        // 🌟 PK로 초고속 조회
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        // [1] 닉네임 변경 (기존 로직 유지)
        if (request.getNickname() != null && !request.getNickname().equals(user.getNickname())) {
            if (userRepository.existsByNickname(request.getNickname())) {
                throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
            }
            user.setNickname(request.getNickname());
        }

        // 🌟 [2] 비밀번호 변경 방어 로직 (핵심 수정 구역)
        // 유저가 '새 비밀번호'를 입력해서 보냈다면?
        if (request.getNewPassword() != null && !request.getNewPassword().trim().isEmpty()) {

            // 팩트 폭격: 현재 비밀번호를 안 보냈거나, DB의 진짜 비밀번호와 일치하지 않으면 강제로 에러를 터뜨림!
            if (request.getCurrentPassword() == null ||
                    !passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {

                // 여기서 에러를 던져야 프론트엔드의 catch(error) 블록으로 빠져서 에러 알람창이 뜹니다.
                throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
            }

            // 위 검문소를 무사히 통과했을 때만, 새 비밀번호를 암호화해서 DB에 덮어씌웁니다.
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }
    }

    // 3. 나의 찜 목록 조회
    @Transactional(readOnly = true)
    public Slice<LikedContentDto> getMyLikedContents(Long id, Pageable pageable) {

        Slice<Likes> likesSlice = likeRepository.findByUserId(id, pageable);

        return likesSlice.map(like -> {
            Content content = like.getContent();

            // 🌟 대기업 클린 코드: DB에 작가 번호(authorId)가 NULL로 들어있을 때의 에러를 원천 차단!
            String authorName = "알 수 없는 작가";
            if (content.getAuthorId() != null) {
                authorName = userRepository.findById(content.getAuthorId())
                        .map(User::getNickname)
                        .orElse("알 수 없는 작가");
            }

            return LikedContentDto.from(content, authorName);
        });
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