package com.untitled.ggobook.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

@Service
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final StringRedisTemplate redisTemplate;

    public EmailService(JavaMailSender javaMailSender, StringRedisTemplate redisTemplate) {
        this.javaMailSender = javaMailSender;
        this.redisTemplate = redisTemplate;
    }

    private String generateAuthCode() {
        SecureRandom random = new SecureRandom();
        return String.format("%06d", random.nextInt(1000000));
    }

    public void sendAuthEmail(String email) {
        String authCode = generateAuthCode();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[GGoBook] 회원가입 이메일 인증 번호입니다.");
        message.setText("인증 번호는 [" + authCode + "] 입니다. 3분 안에 화면에 입력해 주세요.");

        javaMailSender.send(message);
        redisTemplate.opsForValue().set(email, authCode, Duration.ofMinutes(3));
    }

    public boolean verifyAuthCode(String email, String code) {
        String savedCode = redisTemplate.opsForValue().get(email);

        if (savedCode != null && savedCode.equals(code)) {
            redisTemplate.delete(email);
            return true;
        }
        return false;
    }
}