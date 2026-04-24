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

    // 🌟 DB에서 관리자가 아닌 실제 권한을 명시 (ex: "ADMIN" 또는 "ROLE_ADMIN")
    // 만약 Role 타입이 Enum이라면 Role.ADMIN 으로 변경해주세요!
    private final String TARGET_EXCLUDE_ROLE = "ADMIN";

    @Transactional(readOnly = true)
    public Page<UserAdminResponseDto> getMemberList(Pageable pageable) {
        // 🌟 수정: 전체 검색 시 관리자를 제외하는 쿼리 호출
        return userRepository.findByRoleNot(TARGET_EXCLUDE_ROLE, pageable)
                .map(UserAdminResponseDto::new);
    }

    @Transactional(readOnly = true)
    public Page<UserAdminResponseDto> searchMember(String type, String keyword, Pageable pageable) {
        Page<User> users;
        // 🌟 수정: 조건 검색 시에도 관리자를 제외하는 쿼리 호출
        if ("ID".equalsIgnoreCase(type)) {
            users = userRepository.findByUserIdContainingAndRoleNot(keyword, TARGET_EXCLUDE_ROLE, pageable);
        } else if ("NICKNAME".equalsIgnoreCase(type)) {
            users = userRepository.findByNicknameContainingAndRoleNot(keyword, TARGET_EXCLUDE_ROLE, pageable);
        } else {
            users = userRepository.searchAllKeywordAndRoleNot(keyword, TARGET_EXCLUDE_ROLE, pageable);
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

    @Transactional
    public void releaseMember(Long adminId, Long userId, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        user.release();
    }
}