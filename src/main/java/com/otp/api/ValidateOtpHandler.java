package com.otp.api;

import com.google.gson.Gson;
import com.otp.dao.OtpCodeDao;
import com.otp.model.OtpCode;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

public class ValidateOtpHandler implements HttpHandler {
    private static final Logger logger = LoggerFactory.getLogger(ValidateOtpHandler.class);
    private final Gson gson = new Gson();
    private final OtpCodeDao otpCodeDao = new OtpCodeDao();

    static class ValidateRequest {
        public int userId;
        public String code;
    }

    static class ValidateResponse {
        boolean success;
        String message;

        public ValidateResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, new ValidateResponse(false, "Method Not Allowed"));
            return;
        }

        try (InputStream is = exchange.getRequestBody()) {
            String body = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                    .lines()
                    .reduce("", (accumulator, actual) -> accumulator + actual);

            ValidateRequest request = gson.fromJson(body, ValidateRequest.class);

            if (request.userId <= 0 || request.code == null || request.code.isEmpty()) {
                logger.warn("Missing userId or code in request");
                sendResponse(exchange, 400, new ValidateResponse(false, "Missing userId or code"));
                return;
            }

            OtpCode otp = otpCodeDao.findValidCode(request.userId, request.code);

            if (otp == null) {
                logger.info("Invalid or expired OTP code for userId {}", request.userId);
                sendResponse(exchange, 401, new ValidateResponse(false, "Invalid or expired OTP code"));
                return;
            }

            otpCodeDao.markAsUsed(otp.getId());
            logger.info("OTP code validated and marked as used for userId {}", request.userId);

            sendResponse(exchange, 200, new ValidateResponse(true, "OTP code is valid and marked as used"));

        } catch (SQLException e) {
            logger.error("Database error while validating OTP", e);
            sendResponse(exchange, 500, new ValidateResponse(false, "Database error"));
        } catch (Exception e) {
            logger.error("Invalid request format", e);
            sendResponse(exchange, 400, new ValidateResponse(false, "Invalid request format"));
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, ValidateResponse responseObj) throws IOException {
        String responseJson = gson.toJson(responseObj);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, responseJson.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(responseJson.getBytes());
        os.close();
    }
}
