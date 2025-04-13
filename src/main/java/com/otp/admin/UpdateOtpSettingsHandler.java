package com.otp.admin;

import com.google.gson.Gson;
import com.otp.config.OtpConfig;
import com.otp.util.JwtUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateOtpSettingsHandler implements HttpHandler {
    private static final Logger logger = LoggerFactory.getLogger(UpdateOtpSettingsHandler.class);
    private final Gson gson = new Gson();

    @Override
    public void handle(HttpExchange exchange) {
        try {
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
            if (!JwtUtil.isAdminToken(token)) {
                logger.warn("Admin access required");
                sendJson(exchange, 403, "{\"error\":\"Admin access required\"}");
                return;
            }

            InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            Map<String, Double> body = gson.fromJson(reader, Map.class); // Gson читает числа как Double

            int length = body.get("codeLength").intValue();
            int ttl = body.get("codeTTL").intValue();

            OtpConfig.setCodeLength(length);
            OtpConfig.setCodeTTLSeconds(ttl);
            logger.info("OTP settings updated: codeLength={}, codeTTL={}", length, ttl);
            sendJson(exchange, 200, "{\"status\":\"OTP settings updated\"}");
        } catch (Exception e) {
            logger.error("Failed to update OTP settings", e);
            try {
                sendJson(exchange, 500, "{\"error\":\"Internal Server Error\"}");
            } catch (Exception ignored) {
            }
        }
    }

    private void sendJson(HttpExchange exchange, int status, String json) throws java.io.IOException {
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
