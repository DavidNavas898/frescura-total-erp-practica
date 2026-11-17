package com.frescuratotal.erp.common.view;

import java.util.Scanner;

public class MainView {

    private Scanner scanner;

    public MainView(Scanner scanner) {
        this.scanner = scanner;
    }

    public int mostrarMenu() {
        System.out.println();
        System.out.println("=== FRESCURA TOTAL ERP ===");
        System.out.println("1. Proveedores");
        System.out.println("2. Productos");
        System.out.println("3. Inventario (lotes)");
        System.out.println("4. Clientes");
        System.out.println("5. Ventas");
        System.out.println("6. Logistica y entregas");
        System.out.println("7. Generar Reporte de Rendimiento de Proveedores");
        System.out.println("8. Generar Reporte Diario de Compras");
        System.out.println("9. Generar Estadisticas Comerciales");
        System.out.println("0. Salir");
        return leerNumero("Seleccione una opcion: ");
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
}
