package com.otp.api;

import com.google.gson.Gson;
import com.otp.config.OtpConfig;
import com.otp.dao.OtpCodeDao;
import com.otp.dao.UserDao;
import com.otp.model.OtpCode;
import com.otp.model.User;
import com.otp.util.JwtUtil;
import com.otp.util.NotificationService;
import com.otp.util.OtpGenerator;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;

public class GenerateOtpHandler implements HttpHandler {
    private static final Logger logger = LoggerFactory.getLogger(GenerateOtpHandler.class);
    private final Gson gson = new Gson();
    private final OtpCodeDao otpDao = new OtpCodeDao();
    private final UserDao userDao = new UserDao();
    private final NotificationService notificationService = new NotificationService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendJson(exchange, 401, "{\"error\":\"Missing or invalid token\"}");
            return;
        }

        String token = authHeader.substring(7);
        String username = JwtUtil.getUsername(token);

        try {
            User user = userDao.findByUsername(username);
            if (user == null) {
                sendJson(exchange, 403, "{\"error\":\"Invalid user\"}");
                return;
            }

            // Парсинг тела запроса
            String body = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))
                    .lines().reduce("", String::concat);
            Map<String, String> data = gson.fromJson(body, Map.class);

            String channel = data.getOrDefault("channel", "file");
            String destination = data.get("destination");
            String operation = data.get("operation");

            String code = OtpGenerator.generate(OtpConfig.getCodeLength());
            Instant now = Instant.now();
            Instant expiresAt = now.plusSeconds(OtpConfig.getCodeTTLSeconds());

            // Сохраняем в базу
            OtpCode otp = new OtpCode(user.getId(), code, "ACTIVE", now, operation, expiresAt);
            otpDao.save(otp);

            // Отправляем по нужному каналу
            switch (channel.toLowerCase()) {
                case "email" -> notificationService.sendEmail(destination, code);
                case "sms" -> notificationService.sendSmsEmulated(destination, code);
                case "telegram" -> notificationService.sendTelegram(code);
                case "file" -> notificationService.saveToFile(user.getId(), code);
                default -> {
                    logger.warn("Unknown channel: {}", channel);
                    sendJson(exchange, 400, "{\"error\":\"Unknown delivery channel\"}");
                    return;
                }
            }

            logger.info("OTP sent to {} via {}", destination, channel);
            sendJson(exchange, 200, "{\"status\":\"OTP sent via " + channel + "\"}");

        } catch (SQLException e) {
            logger.error("Database error", e);
            sendJson(exchange, 500, "{\"error\":\"Internal server error\"}");
        }
    }

    private void sendJson(HttpExchange exchange, int status, String json) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
