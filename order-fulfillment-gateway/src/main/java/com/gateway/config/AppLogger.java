package com.gateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AppLogger {

    private final Logger logger;

    private AppLogger(Class<?> clazz) {
        this.logger = LoggerFactory.getLogger(clazz);
    }

    public static AppLogger get(Class<?> clazz) {
        return new AppLogger(clazz);
    }

    public void info(String message, Object... args) {
        logger.info(message, args);
    }

    public void warn(String message, Object... args) {
        logger.warn(message, args);
    }

    public void error(String message, Object... args) {
        logger.error(message, args);
    }

    public void error(String message, Throwable throwable) {
        logger.error(message, throwable);
    }

    public void debug(String message, Object... args) {
        logger.debug(message, args);
    }

    /**
     * Standardized banner logging for startup lifecycle and health checks.
     */
    public void status(String stage, String status, String details) {
        logger.info("[{}] -> Status: {} | Details: {}", stage, status, details);
    }
}