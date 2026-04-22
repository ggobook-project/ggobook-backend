package com.untitled.ggobook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Map;

@EnableScheduling
@SpringBootApplication
public class GgobookApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(GgobookApplication.class);
		app.setDefaultProperties(Map.of(
			"spring.servlet.multipart.max-file-size", "100MB",
			"spring.servlet.multipart.max-request-size", "500MB"
		));
		app.run(args);
	}

}
