package com.gateway.patterns.cor;

public class ValidationPipeline {

    private final ValidationHandler firstHandler;

    ValidationPipeline(ValidationHandler firstHandler) {
        this.firstHandler = firstHandler;
    }

    public void validate(ValidationContext context) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null");
        }
        firstHandler.handle(context);
    }
}
