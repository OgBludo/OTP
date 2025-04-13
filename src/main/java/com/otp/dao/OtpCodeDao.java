package com.otp.dao;

import com.otp.db.Database;
import com.otp.model.OtpCode;
import com.otp.model.OtpStatus;
import org.postgresql.util.PGobject;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class OtpCodeDao {

    public void save(OtpCode otp) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO service.otp_codes (user_id, code, created_at, expires_at, status, operation) " +
                            "VALUES (?, ?, ?, ?, ?, ?)"
            );
            stmt.setInt(1, otp.getUserId());
            stmt.setString(2, otp.getCode());
            stmt.setTimestamp(3, Timestamp.from(otp.getCreatedAt()));
            stmt.setTimestamp(4, Timestamp.from(otp.getExpiresAt()));

            PGobject statusObj = new PGobject();
            statusObj.setType("otp_status"); // имя enum-типа в PostgreSQL
            statusObj.setValue(otp.getStatus().name()); // ACTIVE, USED, EXPIRED
            stmt.setObject(5, statusObj);

            stmt.setString(6, otp.getOperation());
            stmt.executeUpdate();
        }
    }


    public OtpCode findValidCode(int userId, String code) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT * FROM service.otp_codes WHERE user_id = ? AND code = ? AND status = 'ACTIVE' AND expires_at > ?"
            );
            stmt.setInt(1, userId);
            stmt.setString(2, code);
            stmt.setTimestamp(3, Timestamp.from(Instant.now()));

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        }
        return null;
    }

    public void markAsUsed(int otpId) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE service.otp_codes SET status = 'USED' WHERE id = ?"
            );
            stmt.setInt(1, otpId);
            stmt.executeUpdate();
        }
    }

    public void expireOldOtps() throws SQLException {
        try (Connection conn = Database.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE service.otp_codes SET status = 'EXPIRED' WHERE expires_at <= ?"
            );
            stmt.setTimestamp(1, Timestamp.from(Instant.now()));
            stmt.executeUpdate();
        }
    }

    public void deleteByUser(int userId) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "DELETE FROM service.otp_codes WHERE user_id = ?"
            );
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }

    public List<OtpCode> getAllByUser(int userId) throws SQLException {
        List<OtpCode> codes = new ArrayList<>();
        try (Connection conn = Database.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT * FROM service.otp_codes WHERE user_id = ?"
            );
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                codes.add(mapRow(rs));
            }
        }
        return codes;
    }

    private OtpCode mapRow(ResultSet rs) throws SQLException {
        OtpCode otp = new OtpCode();
        otp.setId(rs.getInt("id"));
        otp.setUserId(rs.getInt("user_id"));
        otp.setCode(rs.getString("code"));
        otp.setCreatedAt(rs.getTimestamp("created_at").toInstant());
        otp.setExpiresAt(rs.getTimestamp("expires_at").toInstant());
        otp.setStatus(OtpStatus.valueOf(rs.getString("status")));
        otp.setOperation(rs.getString("operation"));
        return otp;
    }
}
