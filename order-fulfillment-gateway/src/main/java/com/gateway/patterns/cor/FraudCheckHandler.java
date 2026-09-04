package com.gateway.patterns.cor;

import com.gateway.exception.PipelineValidationException;

public class FraudCheckHandler extends ValidationHandler {

    @Override
    protected void validate(ValidationContext context) throws PipelineValidationException {
        if (!context.fraudFree()) {
            throw new PipelineValidationException("Fraud validation failed: order was flagged");
        }
    }
}
