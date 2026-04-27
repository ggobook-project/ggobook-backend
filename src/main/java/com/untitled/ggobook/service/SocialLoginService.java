package com.untitled.ggobook.service;

import com.untitled.ggobook.domain.User;
import com.untitled.ggobook.domain.Wallet;
import com.untitled.ggobook.dto.OAuth2Attribute;
import com.untitled.ggobook.repository.UserRepository;
import com.untitled.ggobook.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SocialLoginService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final WalletRepository walletRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. 소셜 데이터 가져오기
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 2. 어느 소셜인지(kakao, naver, google), 키값이 뭔지 가져오기
        String provider = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName();

        // 🌟 3. DTO 번역기 가동! (이 한 줄로 구글/카카오/네이버 데이터가 완벽하게 통일됩니다)
        OAuth2Attribute extractAttributes = OAuth2Attribute.of(provider, userNameAttributeName, oAuth2User.getAttributes());

        // 🌟 4. 통일된 데이터(DTO)를 던져서 회원가입 or 로그인 처리
        User savedUser = processOAuthSignup(extractAttributes);

        // 5. 시큐리티에게 인증 완료 보고
        return new DefaultOAuth2User(
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + savedUser.getRole())),
                extractAttributes.getAttributes(),
                extractAttributes.getAttributeKey()
        );
    }

    // =========================================================================
    // 🌟 소셜 데이터(DTO)를 받아 DB에 저장/업데이트 하는 로직 단 하나만 남겼습니다.
    // =========================================================================
    private User processOAuthSignup(OAuth2Attribute attributes) {
        String loginId = attributes.getProvider() + "_" + attributes.getProviderId();

        return userRepository.findByUserId(loginId)
                .map(user -> {
                    // 이미 가입된 회원이면 이름과 이메일만 최신으로 갱신
                    user.update(attributes.getName(), attributes.getEmail());
                    return userRepository.save(user);
                })
                .orElseGet(() -> {
                    // 신규 회원이면 회원가입 처리
                    User newUser = User.builder()
                            .userId(loginId)
                            .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                            .name(attributes.getName())
                            // 닉네임: 이름_고유ID앞4자리 (최대길이 방어)
                            .nickname(attributes.getName() + "_" + attributes.getProviderId().substring(0, Math.min(4, attributes.getProviderId().length())))
                            .email(attributes.getEmail() != null ? attributes.getEmail() : loginId + "@" + attributes.getProvider() + ".com")
                            .gender("미설정")
                            .role("USER")
                            .build();

                    userRepository.save(newUser);

                    Wallet wallet = new Wallet();
                    wallet.setBalance(0);
                    wallet.setUser(newUser);

                    walletRepository.save(wallet);

                    return newUser;
                });
    }
}