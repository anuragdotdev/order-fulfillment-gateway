package com.gateway.patterns.cor;

import com.gateway.exception.PipelineValidationException;

public abstract class ValidationHandler {

	private ValidationHandler next;

	public ValidationHandler setNext(ValidationHandler next) {
		this.next = next;
		return next;
	}

	public final void handle(ValidationContext context) {
		validate(context);
		if (next != null) {
			next.handle(context);
		}
	}

	protected abstract void validate(ValidationContext context) throws PipelineValidationException;
}
