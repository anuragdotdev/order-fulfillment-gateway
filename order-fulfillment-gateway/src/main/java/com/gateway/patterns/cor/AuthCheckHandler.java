package com.gateway.patterns.cor;

import com.gateway.exception.PipelineValidationException;

public class AuthCheckHandler extends ValidationHandler {

	@Override
	protected void validate(ValidationContext context) throws PipelineValidationException {
		if (!context.authenticated()) {
			throw new PipelineValidationException("Authentication validation failed");
		}
	}
}
