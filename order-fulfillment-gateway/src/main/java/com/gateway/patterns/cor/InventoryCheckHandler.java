package com.gateway.patterns.cor;

import com.gateway.exception.PipelineValidationException;

public class InventoryCheckHandler extends ValidationHandler {

    @Override
    protected void validate(ValidationContext context) throws PipelineValidationException {
        if (!context.inventoryAvailable()) {
            throw new PipelineValidationException("Inventory validation failed: item is unavailable");
        }
    }
}
