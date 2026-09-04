package com.gateway.patterns.cor;

public class ValidationPipelineBuilder {

	public ValidationPipeline build() {
		ValidationHandler auth = new AuthCheckHandler();
		ValidationHandler inventory = new InventoryCheckHandler();
		ValidationHandler fraud = new FraudCheckHandler();

		auth.setNext(inventory).setNext(fraud);
		return new ValidationPipeline(auth);
	}
}
