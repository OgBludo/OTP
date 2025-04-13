package com.otp.util;

import com.otp.service.EmailNotificationService;
import com.otp.service.SmsNotificationService;
import com.otp.service.TelegramNotificationService;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private final EmailNotificationService emailService = new EmailNotificationService();
    private final SmsNotificationService smsService = new SmsNotificationService();
    private final TelegramNotificationService telegramService = new TelegramNotificationService();

    public void sendEmail(String to, String code) {
        logger.info("Sending OTP via email to {}", to);
        emailService.sendCode(to, code);
    }

    public void sendSmsEmulated(String phoneNumber, String code) {
        logger.info("Sending OTP via SMS to {}", phoneNumber);
        smsService.sendCode(phoneNumber, code);
    }

    public void sendTelegram(String code) {
        logger.info("Sending OTP via Telegram");
        telegramService.sendCode("TelegramUser", code); // можно доработать под username
    }

    public void saveToFile(int userId, String code) {
        logger.info("Saving OTP to file for userId {}", userId);
        String filename = "otp_" + userId + "_" + Instant.now().toEpochMilli() + ".txt";
        try (FileWriter fw = new FileWriter(filename)) {
            logger.info("Saving OTP to file for userId {} successful", userId);
            fw.write("Your OTP code: " + code);
        } catch (IOException e) {
            logger.info("Failed to write OTP to file");
            throw new RuntimeException("Failed to write OTP to file", e);
        }
    }
}
