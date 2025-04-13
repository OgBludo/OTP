package com.otp.service;

import org.jsmpp.bean.*;
import org.jsmpp.session.SMPPSession;
import org.jsmpp.session.BindParameter;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmsNotificationService {
    private static final Logger logger = LoggerFactory.getLogger(SmsNotificationService.class);

    private final String host;
    private final int port;
    private final String systemId;
    private final String password;
    private final String systemType;
    private final String sourceAddress;

    public SmsNotificationService() {
        Properties props = loadConfig();
        this.host = props.getProperty("smpp.host");
        this.port = Integer.parseInt(props.getProperty("smpp.port"));
        this.systemId = props.getProperty("smpp.system_id");
        this.password = props.getProperty("smpp.password");
        this.systemType = props.getProperty("smpp.system_type");
        this.sourceAddress = props.getProperty("smpp.source_addr");
    }

    private Properties loadConfig() {
        try {
            Properties props = new Properties();
            props.load(SmsNotificationService.class.getClassLoader()
                    .getResourceAsStream("sms.properties"));
            return props;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load sms configuration", e);
        }
    }

    public void sendCode(String destination, String code) {
        SMPPSession session = new SMPPSession();
        try {
            logger.info("Sending sms to {} with OTP code {}", destination, code);
            session.connectAndBind(host, port, new BindParameter(
                    BindType.BIND_TX, systemId, password, systemType,
                    TypeOfNumber.UNKNOWN, NumberingPlanIndicator.UNKNOWN, null
            ));

            session.submitShortMessage("CMT",
                    TypeOfNumber.INTERNATIONAL, NumberingPlanIndicator.ISDN, sourceAddress,
                    TypeOfNumber.INTERNATIONAL, NumberingPlanIndicator.ISDN, destination,
                    new ESMClass(), (byte) 0, (byte) 1, null, null,
                    new RegisteredDelivery(SMSCDeliveryReceipt.DEFAULT),
                    (byte) 0, DataCodings.ZERO, (byte) 0,
                    ("Your OTP code is: " + code).getBytes());

        } catch (Exception e) {
            logger.info("Failed to send SMS");
            throw new RuntimeException("Failed to send SMS", e);
        } finally {
            session.unbindAndClose();
        }
    }

}