package com.untitled.ggobook.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing // 🌟 이 한 줄이 핵심입니다.
public class JpaConfig {
}