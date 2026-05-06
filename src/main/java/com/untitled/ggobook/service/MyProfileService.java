package com.untitled.ggobook.service;

import com.untitled.ggobook.domain.Content;
import com.untitled.ggobook.domain.User;
import com.untitled.ggobook.dto.MyPostDto;
import com.untitled.ggobook.dto.MyProfileResponse;
import com.untitled.ggobook.dto.UpdateMyInfoRequest;
import com.untitled.ggobook.repository.ContentRepository;
import com.untitled.ggobook.repository.UserRepository;
import com.untitled.ggobook.util.FileUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MyProfileService {

    private final UserRepository userRepository;
    private final ContentRepository contentRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileUtil fileUtil;

    @Transactional(readOnly = true)
    public MyProfileResponse getMyProfile(Long id) {
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
                .profileImageUrl(user.getProfileImageUrl())
                .myPosts(postDtos)
                .build();
    }

    @Transactional
    public void updateMyInfo(Long id, UpdateMyInfoRequest request, org.springframework.web.multipart.MultipartFile file) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        // 1. 프로필 이미지 업데이트 로직
        if (file != null && !file.isEmpty()) {
            // 기존 이미지가 있으면 S3에서 삭제
            if (user.getProfileImageUrl() != null) {
                fileUtil.deleteFromS3(user.getProfileImageUrl());
            }
            // 새 이미지 업로드 및 URL 저장
            user.setProfileImageUrl(fileUtil.uploadToS3(file));
        }

        // 2. 닉네임 업데이트 로직
        if (request.getNickname() != null && !request.getNickname().equals(user.getNickname())) {
            if (userRepository.existsByNickname(request.getNickname())) {
                throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
            }
            user.setNickname(request.getNickname());
        }

        // 3. 비밀번호 업데이트 로직
        if (request.getNewPassword() != null && !request.getNewPassword().trim().isEmpty()) {
            if (request.getCurrentPassword() == null ||
                    !passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
            }
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }
    }

    // ==========================================
    // 🌟 추가: 회원 탈퇴 (Soft Delete)
    // ==========================================
    @Transactional
    public void withdrawUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        // User.java에 만들어둔 즉시 재가입(익명화) 탈퇴 메서드 호출
        user.withdraw();

        // @Transactional 덕분에 더티 체킹이 일어나서 자동으로 DB에 UPDATE 쿼리가 날아갑니다.
    }
}