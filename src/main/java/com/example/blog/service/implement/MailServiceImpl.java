package com.example.blog.service.implement;

import com.example.blog.service.MailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "MAIL-SERVICE")
public class MailServiceImpl implements MailService {

   private final JavaMailSender mailSender;

   @Value("${spring.mail.username}")
   private String from;

    @Override
    @Async("taskExecutor")
    @Retryable(
            retryFor = {MessagingException.class, UnsupportedEncodingException.class},
            backoff = @Backoff(delay = 2000,multiplier = 2))
    public void sendSimpleMail(String to, String subject, String content) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage, true, StandardCharsets.UTF_8.name());
            helper.setTo(to);
            helper.setFrom(from, "Thanh Dev");
            helper.setSubject(subject);
            helper.setText(content, true);
            mailSender.send(mimeMessage);
            log.info("Send email success to {} ", to);
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("Fail to send mail with error: {}", e.getMessage());
        }
    }
}
