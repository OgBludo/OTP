package com.otp;

import com.otp.admin.DeleteUserHandler;
import com.otp.admin.GetUsersHandler;
import com.otp.admin.UpdateOtpSettingsHandler;
import com.otp.api.LoginHandler;
import com.otp.api.RegisterHandler;
import com.otp.api.GenerateOtpHandler;
import com.otp.api.ValidateOtpHandler;
import com.otp.scheduler.OtpExpiryScheduler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class Main {

    public static void main(String[] args) throws IOException {
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        System.out.println("Server started at http://localhost:" + port);
        new OtpExpiryScheduler().start();

        // Тестовый эндпоинт
        server.createContext("/ping", new PingHandler());

        // Public API
        server.createContext("/register", new RegisterHandler());
        server.createContext("/login", new LoginHandler());

        // Admin API
        server.createContext("/admin/config/updateOtpSettings", new UpdateOtpSettingsHandler());
        server.createContext("/admin/users", new GetUsersHandler());
        server.createContext("/admin/users/delete", new DeleteUserHandler());

        // User OTP API
        server.createContext("/otp/generate", new GenerateOtpHandler());
        server.createContext("/otp/validate", new ValidateOtpHandler());

        server.setExecutor(null);
        server.start();
    }

    static class PingHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "{\"status\":\"ok\"}";
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }
}
