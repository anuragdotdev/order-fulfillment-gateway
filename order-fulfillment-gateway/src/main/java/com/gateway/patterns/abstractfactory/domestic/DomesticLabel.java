package com.gateway.patterns.abstractfactory.domestic;

import com.gateway.patterns.abstractfactory.ShippingLabel;

public class DomesticLabel implements ShippingLabel {

    @Override
    public String getDocumentType() {
        return "Domestic shipping label";
    }
}
