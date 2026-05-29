package com.capd.capdbackend.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailSendService {

    private final JavaMailSender mailSender;

    // 임시 비밀번호 발송
    public void sendTempPassword(String toEmail, String tempPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("[CAPD] 임시 비밀번호 안내");
        message.setText(
                "안녕하세요. CAPD 복막투석 관리 시스템입니다.\n\n" +
                        "임시 비밀번호: " + tempPassword + "\n\n" +
                        "로그인 후 반드시 비밀번호를 변경해주세요."
        );

        mailSender.send(message);
        log.info("임시 비밀번호 이메일 발송 완료: {}", toEmail);
    }
}
