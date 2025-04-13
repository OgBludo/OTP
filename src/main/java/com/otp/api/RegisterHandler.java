package com.otp.api;

import com.google.gson.Gson;
import com.otp.dao.UserDao;
import com.otp.model.User;
import com.otp.util.PasswordUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Map;

public class RegisterHandler implements HttpHandler {
    private final Gson gson = new Gson();
    private final UserDao userDao = new UserDao();
    private static final Logger logger = LoggerFactory.getLogger(RegisterHandler.class);

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            return;
        }

        InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
        Map<String, String> data = gson.fromJson(isr, Map.class);

        String username = data.get("username");
        String password = data.get("password");
        String role = data.get("role");
        logger.info("Received registration request: username={}, role={}", username, role);

        if (username == null || password == null || role == null) {
            logger.info("Error occurred while registration (missing fields)");
            sendJson(exchange, 400, "{\"error\":\"Missing fields\"}");
            return;
        }

        try {
            if ("admin".equals(role) && userDao.isAdminExists()) {
                logger.info("Error occurred while registration (admin already exists)");
                sendJson(exchange, 400, "{\"error\":\"Admin already exists\"}");
                return;
            }

            String hash = PasswordUtil.hashPassword(password);
            userDao.save(new User(username, hash, role));
            logger.info("User {} registered successfully", username);
            sendJson(exchange, 200, "{\"status\":\"User registered\"}");
        } catch (SQLException e) {
            logger.error("Registration failed", e);
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
