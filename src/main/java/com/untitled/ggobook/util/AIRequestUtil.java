package com.untitled.ggobook.util;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.util.HashMap;
import java.util.Map;

@Component
public class AIRequestUtil {

    // 파이썬 서버의 주소 (나중에 실제 서버 주소로 변경해야 합니다)
    private final String PYTHON_SERVER_URL = "http://localhost:8000/api/inspect/summary";

    // 외부 서버와 통신하게 해주는 스프링의 기본 도구입니다.
    private final RestTemplate restTemplate;

    public AIRequestUtil(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * 파이썬 서버로 원고를 보내고 요약본을 받아오는 메서드
     * @param description 작품의 원본 줄거리
     * @return AI가 요약한 짧은 줄거리
     */
    public String sendRequest(String description) {
        // 1. 파이썬 서버로 보낼 헤더 설정 (JSON 형식으로 보낸다고 알려줌)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 2. 파이썬 서버로 보낼 알맹이(Body) 데이터 포장
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("text", description); // "text"라는 이름표로 줄거리를 담습니다.

        // 3. 헤더와 알맹이를 하나의 택배 상자(HttpEntity)로 합칩니다.
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            // 4. 파이썬 서버로 POST 요청을 쏘고, 응답을 String 형태로 받아옵니다.
            ResponseEntity<String> response = restTemplate.postForEntity(
                    PYTHON_SERVER_URL,
                    requestEntity,
                    String.class
            );

            // 5. 성공적으로 받아왔다면 요약된 텍스트를 꺼내서 반환합니다.
            return response.getBody();

        } catch (Exception e) {
            // 통신에 실패했을 때
            System.out.println("AI 서버 통신 실패: " + e.getMessage());
            return "요약에 실패했습니다. 관리자에게 문의하세요.";
        }
    }
}
