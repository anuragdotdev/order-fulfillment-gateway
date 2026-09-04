package com.gateway.patterns.abstractfactory.international;

import com.gateway.patterns.abstractfactory.CustomsInvoice;

public class CustomsDeclarationForm implements CustomsInvoice {

    @Override
    public String getDocumentType() {
        return "Customs declaration form";
    }
}
