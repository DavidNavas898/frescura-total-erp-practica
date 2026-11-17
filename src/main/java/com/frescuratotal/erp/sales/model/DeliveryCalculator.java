package com.frescuratotal.erp.sales.model;

public class DeliveryCalculator {

    public double calcularTotalConEntrega(Sale venta, String modalidad) {
        DeliveryStrategy estrategia;
        if (modalidad.equals("URGENTE")) {
            estrategia = new UrgentDelivery();
        } else {
            estrategia = new StandardDelivery();
        }
        return estrategia.calcularTotal(venta.getMontoTotal());
    }
}
