package com.untitled.ggobook.service;

import com.untitled.ggobook.domain.MemberSuspend;
import com.untitled.ggobook.domain.User;
import com.untitled.ggobook.domain.enums.SuspensionDuration;
import com.untitled.ggobook.dto.UserAdminResponseDto;
import com.untitled.ggobook.repository.MemberSuspendRepository;
import com.untitled.ggobook.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminMemberService {

    private final UserRepository userRepository;
    private final MemberSuspendRepository memberSuspendRepository;

    @Transactional(readOnly = true)
    public Page<UserAdminResponseDto> getMemberList(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserAdminResponseDto::new);
    }

    @Transactional(readOnly = true)
    public Page<UserAdminResponseDto> searchMember(String type, String keyword, Pageable pageable) {
        Page<User> users;
        if ("ID".equalsIgnoreCase(type)) {
            users = userRepository.findByUserIdContaining(keyword, pageable);
        } else if ("NICKNAME".equalsIgnoreCase(type)) {
            users = userRepository.findByNicknameContaining(keyword, pageable);
        } else {
            users = userRepository.findByUserIdContainingOrNicknameContaining(keyword, keyword, pageable);
        }
        return users.map(UserAdminResponseDto::new);
    }

    @Transactional
    public void suspendMember(Long adminId, Long userId, SuspensionDuration duration, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("관리자 정보를 찾을 수 없습니다."));

        user.suspend(duration);

        MemberSuspend suspendLog = MemberSuspend.builder()
                .user(user)
                .admin(admin)
                .reason(reason)
                .duration(duration)
                .endDate(user.getSuspensionEndDate())
                .build();

        memberSuspendRepository.save(suspendLog);
    }
}