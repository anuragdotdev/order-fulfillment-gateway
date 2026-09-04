package com.gateway.patterns.abstractfactory.domestic;

import com.gateway.patterns.abstractfactory.CustomsInvoice;
import com.gateway.patterns.abstractfactory.FulfillmentDocsFactory;
import com.gateway.patterns.abstractfactory.ShippingLabel;

public class DomesticFulfillmentFactory implements FulfillmentDocsFactory {

    @Override
    public ShippingLabel createShippingLabel() {
        return new DomesticLabel();
    }

    @Override
    public CustomsInvoice createCustomsInvoice() {
        return new StandardInvoice();
    }
}
