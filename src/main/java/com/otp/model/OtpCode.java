package com.otp.model;

import java.time.Instant;

public class OtpCode {
    private int id;
    private int userId;
    private String code;
    private Instant createdAt;
    private Instant expiresAt;
    private OtpStatus status;
    private String operation;

    public OtpCode() {
    }

    public OtpCode(int userId, String code, String status, Instant createdAt, String operationId, Instant expiresAt) {
        this.userId = userId;
        this.code = code;
        this.status = OtpStatus.valueOf(status); // преобразуем строку в enum
        this.createdAt = createdAt;
        this.operation = operationId;
        this.expiresAt = expiresAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public OtpStatus getStatus() {
        return status;
    }

    public void setStatus(OtpStatus status) {
        this.status = status;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operationId) {
        this.operation = operationId;
    }
}
