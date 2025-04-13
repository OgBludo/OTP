package com.otp.service;

import javax.mail.*;
import javax.mail.internet.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmailNotificationService {
    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationService.class);

    private final String username;
    private final String password;
    private final String fromEmail;
    private final Session session;

    public EmailNotificationService() {
        Properties config = loadConfig();
        this.username = config.getProperty("email.username");
        this.password = config.getProperty("email.password");
        this.fromEmail = config.getProperty("email.from");
        System.out.println(this.username + " " + this.password + " " + this.fromEmail);
        this.session = Session.getInstance(config, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    private Properties loadConfig() {
        try {
            Properties props = new Properties();
            props.load(EmailNotificationService.class.getClassLoader()
                    .getResourceAsStream("email.properties"));
            return props;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load email configuration", e);
        }
    }

    public void sendCode(String toEmail, String code) {
        try {
            logger.info("Sending email to {} with OTP code {}", toEmail, code);
            session.setDebug(true);
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            message.setSubject("Your OTP Code");
            message.setText("Your verification code is: " + code);
            Transport.send(message);
            System.out.println(toEmail);
        } catch (MessagingException e) {
            logger.info("Failed to send email");
            throw new RuntimeException("Failed to send email", e);
        }
    }
}