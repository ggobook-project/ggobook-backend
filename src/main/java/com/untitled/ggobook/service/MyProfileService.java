package com.untitled.ggobook.service;

import com.untitled.ggobook.domain.Content;
import com.untitled.ggobook.domain.User;
import com.untitled.ggobook.dto.MyPostDto;
import com.untitled.ggobook.dto.MyProfileResponse;
import com.untitled.ggobook.dto.UpdateMyInfoRequest;
import com.untitled.ggobook.repository.ContentRepository;
import com.untitled.ggobook.repository.UserRepository;
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
                .myPosts(postDtos)
                .build();
    }

    @Transactional
    public void updateMyInfo(Long id, UpdateMyInfoRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        if (request.getNickname() != null && !request.getNickname().equals(user.getNickname())) {
            if (userRepository.existsByNickname(request.getNickname())) {
                throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
            }
            user.setNickname(request.getNickname());
        }

        if (request.getNewPassword() != null && !request.getNewPassword().trim().isEmpty()) {
            if (request.getCurrentPassword() == null ||
                    !passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
            }
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }
    }
}