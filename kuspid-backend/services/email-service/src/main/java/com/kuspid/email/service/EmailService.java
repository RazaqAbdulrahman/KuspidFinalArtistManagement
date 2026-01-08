package com.kuspid.email.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    /**
     * Sends an email asynchronously.
     * Supports HTML content for rich notifications.
     */
    @Async
    public void sendEmail(String to, String subject, String content, boolean isHtml) {
        log.info("Queuing email dispatch to: {} | Subject: {}", to, subject);

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name());

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, isHtml);

            // In a real Netflix-scale app, we might check a suppression list here
            mailSender.send(mimeMessage);

            log.info("Email successfully dispatched to {}", to);
        } catch (Exception e) {
            log.error("SMTP Persistent Failure. Failed to send email to {}. Trace: ", to, e);
            // In production, we would likely push this to a Dead Letter Queue (DLQ) for
            // retry
        }
    }

    // Overload for backward compatibility and simpler text emails
    public void sendEmail(String to, String subject, String text) {
        sendEmail(to, subject, text, false);
    }
}
