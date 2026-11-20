package com.frescuratotal.erp.sales.model;

// Esto pertenece al punto 3 del taller de practica
public class UrgentDelivery implements DeliveryStrategy {

    private static final double RECARGO = 0.12;

    @Override
    public double calcularTotal(double valorCompra) {
        return Math.round((valorCompra + valorCompra * RECARGO) * 100) / 100.0;
    }
}
