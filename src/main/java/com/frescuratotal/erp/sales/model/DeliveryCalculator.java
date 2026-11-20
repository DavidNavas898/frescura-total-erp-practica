package com.frescuratotal.erp.sales.model;

// Esto pertenece al punto 3 del taller de practica
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
