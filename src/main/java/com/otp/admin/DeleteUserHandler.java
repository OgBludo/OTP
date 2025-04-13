package com.otp.admin;

import com.google.gson.Gson;
import com.otp.dao.UserDao;
import com.otp.util.JwtUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class DeleteUserHandler implements HttpHandler {
    private static final Logger logger = LoggerFactory.getLogger(DeleteUserHandler.class);
    private final Gson gson = new Gson();
    private final UserDao userDao = new UserDao();

    @Override
    public void handle(HttpExchange exchange) {
        logger.info("Received request to delete user");
        try {
            if (!"DELETE".equalsIgnoreCase(exchange.getRequestMethod())) {
                logger.warn("Method not allowed: {}", exchange.getRequestMethod());
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                logger.warn("Missing or invalid token");
                sendJson(exchange, 401, "{\"error\":\"Missing or invalid token\"}");
                return;
            }

            String token = authHeader.substring(7);
            if (!JwtUtil.isAdminToken(token)) {
                logger.warn("Admin access required but token not valid");
                sendJson(exchange, 403, "{\"error\":\"Admin access required\"}");
                return;
            }

            InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            Map<String, String> body = gson.fromJson(reader, Map.class);

            String username = body.get("username");
            if (username == null) {
                logger.warn("Username required but not provided");
                sendJson(exchange, 400, "{\"error\":\"Username required\"}");
                return;
            }
            logger.info("Attempting to delete user: {}", username);
            boolean deleted = userDao.deleteUserAndOtps(username);
            if (deleted) {
                logger.info("User {} deleted successfully", username);
                sendJson(exchange, 200, "{\"status\":\"User deleted\"}");
            } else {
                logger.warn("User {} not found", username);
                sendJson(exchange, 404, "{\"error\":\"User not found\"}");
            }

        } catch (Exception e) {
            logger.error("Error while processing delete user request", e);
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
