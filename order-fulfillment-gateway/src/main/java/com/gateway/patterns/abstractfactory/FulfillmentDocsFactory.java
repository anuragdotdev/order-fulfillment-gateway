package com.gateway.patterns.abstractfactory;

public interface FulfillmentDocsFactory {

	ShippingLabel createShippingLabel();

	CustomsInvoice createCustomsInvoice();
}
