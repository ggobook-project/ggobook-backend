package com.untitled.ggobook.util;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.util.Map;

@Component
public class AIRequestUtil {

    @Value("${app.llm-url}")
    private String llmUrl;

    private final RestTemplate restTemplate;

    public AIRequestUtil(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String sendRequest(String description) {
        // 🌟 수정 포인트 1: 메서드 내부에서 URL을 조립해야 llmUrl 값이 정상적으로 들어옵니다.
        String contentSummaryUrl = llmUrl + "/api/inspect/summary";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, String> requestBody = Map.of("text", description);
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(contentSummaryUrl, requestEntity, String.class);
            return response.getBody();
        } catch (Exception e) {
            // 🌟 수정 포인트 2: 에러를 절대 삼키지 마세요! 콘솔에서 무조건 확인해야 합니다.
            System.err.println("🚨 AI 요약(Content) 통신 에러 발생!");
            e.printStackTrace();
            return "요약에 실패했습니다.";
        }
    }

    public String requestRelaySummary(String entryText) {
        // 🌟 여기도 마찬가지로 내부에서 조립합니다.
        String relaySummaryUrl = llmUrl + "/api/relay/summarize";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, String> requestBody = Map.of("entry_text", entryText);
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<RelayResponse> response = restTemplate.postForEntity(relaySummaryUrl, requestEntity, RelayResponse.class);
            if (response.getBody() != null) return response.getBody().getSafe_summary();
        } catch (Exception e) {
            System.err.println("🚨 AI 요약(Relay) 통신 에러 발생!");
            e.printStackTrace();
        }
        return "가이드라인 위반으로 블라인드 처리되었습니다.";
    }

    @Data
    public static class RelayResponse {
        private String safe_summary;
    }
}