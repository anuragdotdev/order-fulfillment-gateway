package com.gateway.patterns.abstractfactory.international;

import com.gateway.patterns.abstractfactory.CustomsInvoice;
import com.gateway.patterns.abstractfactory.FulfillmentDocsFactory;
import com.gateway.patterns.abstractfactory.ShippingLabel;

public class InternationalFulfillmentFactory implements FulfillmentDocsFactory {

    @Override
    public ShippingLabel createShippingLabel() {
        return new InternationalLabel();
    }

    @Override
    public CustomsInvoice createCustomsInvoice() {
        return new CustomsDeclarationForm();
    }
}
