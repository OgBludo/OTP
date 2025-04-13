package com.otp.scheduler;

import com.otp.dao.OtpCodeDao;

import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OtpExpiryScheduler {
    private static final Logger log = LoggerFactory.getLogger(OtpExpiryScheduler.class);
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final OtpCodeDao otpCodeDao = new OtpCodeDao();

    public void start() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                otpCodeDao.expireOldOtps();
                log.info("Expired old OTPs");
                System.out.println("[OTP Scheduler] Expired old OTPs");
            } catch (SQLException e) {
                e.printStackTrace();
                log.error("Failed to expire OTPs", e);
            }
        }, 0, 1, TimeUnit.MINUTES); // Интервал проверки — 1 минута
    }

    public void stop() {
        scheduler.shutdownNow();
    }
}
