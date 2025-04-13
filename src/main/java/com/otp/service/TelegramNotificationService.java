package com.otp.service;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TelegramNotificationService {
    private final String apiUrl;
    private final String chatId;
    private static final Logger logger = LoggerFactory.getLogger(TelegramNotificationService.class);

    public TelegramNotificationService() {
        Properties props = loadConfig();
        this.apiUrl = props.getProperty("telegram.api.url");
        this.chatId = props.getProperty("telegram.chat.id");
    }

    private Properties loadConfig() {
        try {
            Properties props = new Properties();
            props.load(TelegramNotificationService.class.getClassLoader()
                    .getResourceAsStream("telegram.properties"));
            return props;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load telegram configuration", e);
        }
    }

    public void sendCode(String ignored, String code) {
        logger.info("Sending message via telegram with OTP code {}", code);

        try {
            String message = "🔐 Your OTP code is: " + code;

            String url = String.format("%s?chat_id=%s&text=%s",
                    apiUrl,
                    chatId,
                    URLEncoder.encode(message, StandardCharsets.UTF_8));

            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpGet request = new HttpGet(url);
                try (CloseableHttpResponse response = httpClient.execute(request)) {
                    int status = response.getStatusLine().getStatusCode();
                    if (status != 200) {
                        logger.info("Telegram API error. Status code: " + status);
                        System.err.println("Telegram API error. Status code: " + status);
                    } else {
                        logger.info("Telegram message sent successfully.");
                        System.out.println("Telegram message sent successfully.");
                    }
                }
            }
        } catch (Exception e) {
            logger.info("Failed to send Telegram message");

            throw new RuntimeException("Failed to send Telegram message", e);
        }
    }
}
