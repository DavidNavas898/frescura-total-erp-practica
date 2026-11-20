package com.frescuratotal.erp.inventory.view;

import com.frescuratotal.erp.inventory.model.Lot;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

// Esto pertenece al punto 2 del taller de practica
public class PurchaseCsv {

    public String generarCsv(List<Lot> compras, LocalDate fecha) throws IOException {
        String nombreArchivo = "compras_" + fecha + ".csv";

        FileWriter writer = new FileWriter(nombreArchivo);
        writer.write("NumeroCompra;NombreProveedor;MontoTotal\n");
        for (Lot compra : compras) {
            double monto = compra.getPrecioCompra() * compra.getCantidadInicial();
            writer.write(compra.getIdLote() + ";" + compra.getNombreProveedor() + ";" + formatearMonto(monto) + "\n");
        }
        writer.close();

        return nombreArchivo;
    }

    private String formatearMonto(double monto) {
        long centavosTotales = Math.round(monto * 100);
        long pesos = centavosTotales / 100;
        long centavos = centavosTotales % 100;
        if (centavos < 10) {
            return pesos + ".0" + centavos;
        }
        return pesos + "." + centavos;
    }
}
