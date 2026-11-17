package com.frescuratotal.erp.sales.model;

public class StandardDelivery implements DeliveryStrategy {

    private static final double RECARGO = 0.0;

    @Override
    public double calcularTotal(double valorCompra) {
        return Math.round((valorCompra + valorCompra * RECARGO) * 100) / 100.0;
    }
}
