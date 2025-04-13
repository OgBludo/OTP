package com.otp.api;

import com.google.gson.Gson;
import com.otp.dao.UserDao;
import com.otp.model.User;
import com.otp.util.JwtUtil;
import com.otp.util.PasswordUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginHandler implements HttpHandler {

    private final Gson gson = new Gson();
    private final UserDao userDao = new UserDao();
    private static final Logger logger = LoggerFactory.getLogger(LoginHandler.class);

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
        Map<String, String> data = gson.fromJson(isr, Map.class);

        String username = data.get("username");
        String password = data.get("password");
        logger.info("Received login request: username={}", username);

        if (username == null || password == null) {
            logger.info("Error occurred while login (missing fields)");
            sendJson(exchange, 400, "{\"error\":\"Missing fields\"}");
            return;
        }

        try {
            User user = userDao.findByUsername(username);
            if (user == null || !PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
                logger.info("Error occurred while login (Invalid credentials for {})", username);
                sendJson(exchange, 401, "{\"error\":\"Invalid credentials\"}");
                return;
            }

            try {
                String token = JwtUtil.generateToken(user);
                System.out.println("✅ Token: " + token);
                logger.info("Login successful, token generated for {}", username);
                sendJson(exchange, 200, "{\"token\":\"" + token + "\"}");
            } catch (Exception e) {
                logger.error("Token generation failed", e);
                sendJson(exchange, 500, "{\"error\":\"Token generation failed\"}");
            }

        } catch (SQLException e) {
            logger.info("Error occurred while login" + e.getMessage());
            sendJson(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private void sendJson(HttpExchange exchange, int status, String response) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
