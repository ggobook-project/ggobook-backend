package com.untitled.ggobook.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration // 이 클래스는 스프링의 설정 파일임을 알려줍니다.
public class RestTemplateConfig {

    @Bean // 이제 스프링이 이 메서드를 보고 RestTemplate 객체를 미리 만들어둡니다.
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
