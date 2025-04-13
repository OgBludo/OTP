package com.otp.admin;

import com.google.gson.Gson;
import com.otp.dao.UserDao;
import com.otp.model.User;
import com.otp.util.JwtUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class GetUsersHandler implements HttpHandler {
    private static final Logger logger = LoggerFactory.getLogger(GetUsersHandler.class);
    private final Gson gson = new Gson();
    private final UserDao userDao = new UserDao();

    @Override
    public void handle(HttpExchange exchange) {
        logger.info("Received request to get users");
        try {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                logger.warn("Invalid method: {}", exchange.getRequestMethod());
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
                logger.warn("Admin token invalid");
                sendJson(exchange, 403, "{\"error\":\"Admin access required\"}");
                return;
            }

            List<User> users = userDao.findAllNonAdmins();
            logger.info("Fetched {} users", users.size());
            String json = gson.toJson(users);
            sendJson(exchange, 200, json);

        } catch (Exception e) {
            logger.error("Error while getting users", e);
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
