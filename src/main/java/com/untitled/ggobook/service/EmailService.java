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
    // 비밀번호 재설정 링크 발송 전용 메서드
    public void sendPasswordResetLink(String email, String resetToken) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[GGoBook] 비밀번호 재설정 안내");

        // 프론트엔드 라우팅 주소 (팀 프로젝트 시 도메인으로 변경)
        String resetUrl = "http://localhost:5173/reset-password?token=" + resetToken;

        message.setText("비밀번호를 재설정하려면 아래 링크를 클릭해 주세요.\n\n"
                + resetUrl + "\n\n"
                + "(이 링크는 30분 동안만 유효합니다.)");

        javaMailSender.send(message);
    }
    // 추가: 전체 아이디 발송 전용 메서드
    public void sendFullIdEmail(String email, String fullId) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[GGoBook] 요청하신 아이디 찾기 결과입니다.");
        message.setText("회원님의 전체 아이디는 [" + fullId + "] 입니다.\n" +
                "로그인 후 다양한 서비스를 이용해 주세요.");

        javaMailSender.send(message);
    }



}