package com.gateway.patterns.cor;

public record ValidationContext(boolean authenticated, boolean inventoryAvailable, boolean fraudFree) {
}
