package com.untitled.ggobook.dto;

import lombok.Builder;
import lombok.Getter;
import java.util.Map;

@Getter
@Builder
public class OAuth2Attribute {

    private Map<String, Object> attributes;
    private String attributeKey;
    private String provider;
    private String providerId;
    private String email;
    private String name;

    public static OAuth2Attribute of(String provider, String attributeKey, Map<String, Object> attributes) {
        if ("kakao".equals(provider)) {
            return ofKakao(provider, "id", attributes);
        } else if ("naver".equals(provider)) {
            return ofNaver(provider, "id", attributes);
        }
        return ofGoogle(provider, attributeKey, attributes);
    }

    private static OAuth2Attribute ofGoogle(String provider, String attributeKey, Map<String, Object> attributes) {
        return OAuth2Attribute.builder()
                .provider(provider)
                .providerId(String.valueOf(attributes.get(attributeKey)))
                .name((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .attributes(attributes)
                .attributeKey(attributeKey)
                .build();
    }

    private static OAuth2Attribute ofKakao(String provider, String attributeKey, Map<String, Object> attributes) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> kakaoProfile = (Map<String, Object>) kakaoAccount.get("profile");

        return OAuth2Attribute.builder()
                .provider(provider)
                .providerId(String.valueOf(attributes.get(attributeKey)))
                .name((String) kakaoProfile.get("nickname"))
                .email((String) kakaoAccount.get("email"))
                .attributes(attributes)
                .attributeKey(attributeKey)
                .build();
    }

    private static OAuth2Attribute ofNaver(String provider, String attributeKey, Map<String, Object> attributes) {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");

        return OAuth2Attribute.builder()
                .provider(provider)
                .providerId((String) response.get(attributeKey))
                .name((String) response.get("name"))
                .email((String) response.get("email"))
                .attributes(response)
                .attributeKey(attributeKey)
                .build();
    }
}