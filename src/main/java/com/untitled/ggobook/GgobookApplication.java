package com.untitled.ggobook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class GgobookApplication {

	public static void main(String[] args) {
		SpringApplication.run(GgobookApplication.class, args);
	}

}
