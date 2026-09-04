package com.gateway.patterns.abstractfactory.domestic;

import com.gateway.patterns.abstractfactory.CustomsInvoice;

public class StandardInvoice implements CustomsInvoice {

    @Override
    public String getDocumentType() {
        return "Standard invoice";
    }
}
