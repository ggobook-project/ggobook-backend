package com.untitled.ggobook;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Map;
import java.util.TimeZone;

@EnableScheduling
@SpringBootApplication
public class GgobookApplication {

	@PostConstruct // 🌟 [추가] 애플리케이션 시작 시 타임존을 한국(KST)으로 고정합니다.
	public void started() {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
	}

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(GgobookApplication.class);

		// 기존의 멀티파트 설정 유지
		app.setDefaultProperties(Map.of(
				"spring.servlet.multipart.max-file-size", "100MB",
				"spring.servlet.multipart.max-request-size", "500MB"
		));

		app.run(args);
	}

}