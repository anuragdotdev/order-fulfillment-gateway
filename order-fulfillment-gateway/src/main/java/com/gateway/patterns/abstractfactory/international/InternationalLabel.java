package com.gateway.patterns.abstractfactory.international;

import com.gateway.patterns.abstractfactory.ShippingLabel;

public class InternationalLabel implements ShippingLabel {

    @Override
    public String getDocumentType() {
        return "International shipping label";
    }
}
