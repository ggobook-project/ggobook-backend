package com.untitled.ggobook.util;

import lombok.Data;
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

    private final String CONTENT_SUMMARY_URL = "http://localhost:8000/api/inspect/summary";
    private final String RELAY_SUMMARY_URL = "http://localhost:8000/api/relay/summarize";

    private final RestTemplate restTemplate;

    public AIRequestUtil(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // [기존 유지] 작품 줄거리 요약 (아무것도 수정 안 해도 됨!)
    public String sendRequest(String description) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, String> requestBody = Map.of("text", description);
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(CONTENT_SUMMARY_URL, requestEntity, String.class);
            return response.getBody();
        } catch (Exception e) {
            return "요약에 실패했습니다.";
        }
    }

    // [신규 추가] 릴레이 전용 요약 (안전한 DTO 처리)
    public String requestRelaySummary(String entryText) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, String> requestBody = Map.of("entry_text", entryText);
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            // 이번에는 RelayResponse DTO를 사용하여 JSON 알맹이만 쏙 뽑아냅니다.
            ResponseEntity<RelayResponse> response = restTemplate.postForEntity(RELAY_SUMMARY_URL, requestEntity, RelayResponse.class);
            if (response.getBody() != null) return response.getBody().getSafe_summary();
        } catch (Exception e) {
            System.err.println("🚨 릴레이 요약 실패: " + e.getMessage());
        }
        return "가이드라인 위반으로 블라인드 처리되었습니다.";
    }

    @Data
    public static class RelayResponse {
        private String safe_summary;
    }
}
