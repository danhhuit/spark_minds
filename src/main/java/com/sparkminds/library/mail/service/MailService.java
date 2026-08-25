package com.sparkminds.library.mail.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

        private final JavaMailSender mailSender;

        @Value("${app.frontend-url}")
        private String frontendUrl;

        @Value("${app.mail-from}")
        private String mailFrom;

        public void sendVerificationEmail(
                        String recipient,
                        String rawToken) {
                String verificationUrl = frontendUrl
                                + "/?token="
                                + rawToken;

                SimpleMailMessage message = new SimpleMailMessage();

                message.setFrom(mailFrom);
                message.setTo(recipient);
                message.setSubject(
                                "Verify your library account");
                message.setText(
                                "Welcome to Library Management!\n\n"
                                                + "Click the link below to verify "
                                                + "your account:\n"
                                                + verificationUrl
                                                + "\n\nThis link expires in 24 hours.");

                mailSender.send(message);
        }

        public void sendPasswordResetEmail(
                        String recipient,
                        String rawToken) {
                String resetUrl = frontendUrl
                                + "/?resetToken="
                                + rawToken;

                SimpleMailMessage message = new SimpleMailMessage();

                message.setFrom(mailFrom);
                message.setTo(recipient);
                message.setSubject(
                                "Đặt lại mật khẩu Spark Library");
                message.setText(
                                "Hệ thống vừa nhận được yêu cầu đặt lại "
                                                + "mật khẩu cho tài khoản của bạn.\n\n"
                                                + "Nhấn vào liên kết dưới đây để "
                                                + "tạo mật khẩu mới:\n"
                                                + resetUrl
                                                + "\n\nLiên kết có hiệu lực trong 30 phút.\n"
                                                + "Nếu bạn không gửi yêu cầu này, "
                                                + "hãy bỏ qua email.");

                mailSender.send(message);
        }

        public void sendEmailChangeCode(
                        String newEmail,
                        String verificationCode) {
                SimpleMailMessage message = new SimpleMailMessage();

                message.setFrom(mailFrom);
                message.setTo(newEmail);
                message.setSubject(
                                "Verify your new library email");
                message.setText(
                                "You requested to change your "
                                                + "library account email.\n\n"
                                                + "Your verification code is:\n\n"
                                                + verificationCode
                                                + "\n\nThis code expires in 10 minutes."
                                                + "\nYou have a maximum of 5 attempts."
                                                + "\n\nIf you did not request this, "
                                                + "ignore this email.");

                mailSender.send(message);
        }
}
