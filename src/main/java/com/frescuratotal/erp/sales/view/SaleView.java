package com.frescuratotal.erp.sales.view;

import com.frescuratotal.erp.inventory.model.Lot;
import com.frescuratotal.erp.inventory.model.Product;
import com.frescuratotal.erp.sales.model.Customer;
import com.frescuratotal.erp.sales.model.Sale;
import com.frescuratotal.erp.sales.model.SaleDetail;
import java.util.List;
import java.util.Scanner;

public class SaleView {

    private Scanner scanner;

    public SaleView(Scanner scanner) {
        this.scanner = scanner;
    }

    public int mostrarMenu() {
        System.out.println();
        System.out.println("--- MENU DE VENTAS ---");
        System.out.println("1. Nueva venta");
        System.out.println("2. Enviar venta a preparacion");
        System.out.println("0. Volver al menu principal");
        return leerNumero("Seleccione una opcion: ");
    }

    public void mostrarClientes(List<Customer> clientes) {
        System.out.println();
        System.out.println("--- CLIENTES ---");
        for (Customer cliente : clientes) {
            System.out.println(cliente.getIdCliente() + " - " + cliente.getNombre()
                    + " (" + cliente.getNombreTipo() + ")");
        }
    }

    public void mostrarProductos(List<Product> productos) {
        System.out.println();
        System.out.println("--- PRODUCTOS ---");
        for (Product producto : productos) {
            System.out.println(producto.getIdProducto() + " - " + producto.getNombre()
                    + " (" + producto.getUnidadMedida() + ")");
        }
    }

    public void mostrarLotesDisponibles(List<Lot> lotes) {
        System.out.println();
        System.out.println("--- LOTES DISPONIBLES (FEFO: primero el que vence antes) ---");
        for (Lot lote : lotes) {
            String promocion = "";
            if (lote.getIdEstado() == 2) {
                promocion = " | PROMOCION 50%";
            }
            System.out.println("Lote: " + lote.getNumeroLote()
                    + " | Vence: " + lote.getFechaVencimiento()
                    + " | Cantidad: " + lote.getCantidad()
                    + " | Precio: " + lote.getPrecioVenta()
                    + " | Estado: " + lote.getNombreEstado()
                    + promocion);
        }
    }

    public void mostrarVentas(List<Sale> ventas) {
        System.out.println();
        System.out.println("--- VENTAS REGISTRADAS ---");
        if (ventas.isEmpty()) {
            System.out.println("No hay ventas en estado Registrada.");
            return;
        }
        for (Sale venta : ventas) {
            System.out.println("ID: " + venta.getIdVenta()
                    + " | Venta: " + venta.getNumeroVenta()
                    + " | Cliente: " + venta.getNombreCliente()
                    + " | Fecha: " + venta.getFechaVenta()
                    + " | Total: " + venta.getMontoTotal()
                    + " | Estado: " + venta.getNombreEstado());
        }
    }

    public int pedirIdVenta() {
        return leerNumero("ID de la venta: ");
    }

    public int pedirIdCliente() {
        return leerNumero("ID del cliente: ");
    }

    public int pedirIdProducto() {
        return leerNumero("ID del producto (0 para terminar el pedido): ");
    }

    public double pedirCantidad() {
        return leerDecimal("Cantidad a vender: ");
    }

    public String pedirVendedor() {
        System.out.print("Nombre del vendedor: ");
        return scanner.nextLine();
    }

    public void mostrarResumen(Sale venta) {
        System.out.println();
        System.out.println("--- RESUMEN DEL PEDIDO ---");
        for (SaleDetail detalle : venta.getDetalles()) {
            System.out.println(detalle.getNombreProducto()
                    + " | Lote: " + detalle.getNumeroLote()
                    + " | Cantidad: " + detalle.getCantidadVendida()
                    + " | Precio: " + detalle.getPrecioUnitario()
                    + " | Descuento: " + detalle.getMontoDescuento()
                    + " | IVA: " + detalle.getMontoImpuesto()
                    + " | Total linea: " + detalle.getTotalLinea());
        }
        System.out.println("Subtotal: " + venta.getSubtotal());
        System.out.println("Descuentos: " + venta.getTotalDescuento());
        System.out.println("IVA: " + venta.getTotalImpuesto());
        System.out.println("TOTAL A PAGAR: " + venta.getMontoTotal());
    }

    public String pedirModalidadEntrega() {
        System.out.println();
        System.out.println("Modalidad de entrega:");
        System.out.println("1. ESTANDAR (sin recargo)");
        System.out.println("2. URGENTE (recargo del 12%)");
        int opcion = leerNumero("Seleccione la modalidad: ");
        while (opcion != 1 && opcion != 2) {
            System.out.println("Opcion invalida. Debe ser 1 o 2.");
            opcion = leerNumero("Seleccione la modalidad: ");
        }
        if (opcion == 2) {
            return "URGENTE";
        }
        return "ESTANDAR";
    }

    public void mostrarEstadisticas(String categoria, double unidades, List<String> clientesConDevoluciones) {
        System.out.println();
        System.out.println("===== ESTADISTICAS COMERCIALES =====");
        System.out.println("Categoria con mayor volumen vendido: " + categoria + " - " + unidades + " unidades");
        System.out.println("Clientes con devoluciones:");
        clientesConDevoluciones.forEach(nombre -> System.out.println("- " + nombre));
    }

    public boolean confirmar(String mensaje) {
        System.out.print(mensaje + " (s/n): ");
        String respuesta = scanner.nextLine();
        return respuesta.equalsIgnoreCase("s");
    }

    public void mostrarMensaje(String mensaje) {
        System.out.println(mensaje);
    }

    private int leerNumero(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String texto = scanner.nextLine();
            try {
                return Integer.parseInt(texto);
            } catch (NumberFormatException e) {
                System.out.println("Debe ingresar un numero.");
            }
        }
    }

    private double leerDecimal(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String texto = scanner.nextLine();
            try {
                return Double.parseDouble(texto);
            } catch (NumberFormatException e) {
                System.out.println("Debe ingresar un valor numerico (ej: 2.5).");
            }
        }
    }
}
