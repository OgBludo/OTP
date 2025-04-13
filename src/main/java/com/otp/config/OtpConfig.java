package com.otp.config;

import com.otp.db.Database;

import java.sql.*;

public class OtpConfig {

    private static final int DEFAULT_CODE_LENGTH = 6;
    private static final int DEFAULT_TTL = 180;

    public static int getCodeLength() {
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT code_length FROM otp_config WHERE id = 1")) {

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("code_length");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return DEFAULT_CODE_LENGTH;
    }

    public static int getCodeTTLSeconds() {
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT expiration_time FROM otp_config WHERE id = 1")) {

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("expiration_time");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return DEFAULT_TTL;
    }

    public static void setCodeLength(int length) {
        updateConfig(length, getCodeTTLSeconds());
    }

    public static void setCodeTTLSeconds(int ttl) {
        updateConfig(getCodeLength(), ttl);
    }

    private static void updateConfig(int length, int ttl) {
        try (Connection conn = Database.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO otp_config (id, code_length, expiration_time) " +
                            "VALUES (1, ?, ?) " +
                            "ON CONFLICT (id) DO UPDATE " +
                            "SET code_length = EXCLUDED.code_length, expiration_time = EXCLUDED.expiration_time");

            stmt.setInt(1, length);
            stmt.setInt(2, ttl);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}