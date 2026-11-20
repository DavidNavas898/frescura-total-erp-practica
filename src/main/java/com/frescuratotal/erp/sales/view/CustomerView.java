package com.frescuratotal.erp.sales.view;

import com.frescuratotal.erp.sales.model.Customer;
import java.util.List;
import java.util.Scanner;

public class CustomerView {

    private Scanner scanner;

    public CustomerView(Scanner scanner) {
        this.scanner = scanner;
    }

    public int mostrarMenu() {
        System.out.println();
        System.out.println("--- MENU DE CLIENTES ---");
        System.out.println("1. Listar clientes");
        System.out.println("2. Registrar cliente");
        System.out.println("3. Buscar cliente por ID");
        System.out.println("4. Importar Clientes desde Archivo");
        System.out.println("0. Volver al menu principal");
        return leerNumero("Seleccione una opcion: ");
    }

    // Esto pertenece al punto 1 del taller de practica
    public int pedirIdCliente() {
        System.out.println();
        return leerNumero("ID del cliente: ");
    }

    public void mostrarCliente(Customer cliente) {
        System.out.println("ID: " + cliente.getIdCliente()
                + " | Nombre: " + cliente.getNombre()
                + " | Documento: " + cliente.getDocumento()
                + " | Tipo: " + cliente.getNombreTipo()
                + " | Telefono: " + cliente.getTelefono()
                + " | Email: " + cliente.getEmail());
    }

    public void mostrarClientes(List<Customer> clientes) {
        System.out.println();
        System.out.println("--- LISTA DE CLIENTES ---");
        if (clientes.isEmpty()) {
            System.out.println("No hay clientes registrados.");
            return;
        }
        for (Customer cliente : clientes) {
            System.out.println("ID: " + cliente.getIdCliente()
                    + " | Nombre: " + cliente.getNombre()
                    + " | Documento: " + cliente.getDocumento()
                    + " | Tipo: " + cliente.getNombreTipo()
                    + " | Telefono: " + cliente.getTelefono()
                    + " | Email: " + cliente.getEmail()
                    + " | Direccion: " + cliente.getDireccion()
                    + " | Registro: " + cliente.getFechaRegistro());
        }
    }

    public Customer pedirDatosCliente() {
        System.out.println();
        System.out.println("--- REGISTRAR NUEVO CLIENTE ---");
        Customer cliente = new Customer();

        System.out.print("Nombre: ");
        cliente.setNombre(scanner.nextLine());

        System.out.print("Documento (cedula o NIT): ");
        cliente.setDocumento(scanner.nextLine());

        System.out.print("Email: ");
        cliente.setEmail(scanner.nextLine());

        System.out.print("Telefono: ");
        cliente.setTelefono(scanner.nextLine());

        System.out.print("Direccion: ");
        cliente.setDireccion(scanner.nextLine());

        System.out.println("Tipos de cliente:");
        System.out.println("1 - Mayorista");
        System.out.println("2 - Minorista");
        int tipo = leerNumero("ID del tipo de cliente: ");
        while (tipo != 1 && tipo != 2) {
            System.out.println("Tipo no valido. Debe ser 1 o 2.");
            tipo = leerNumero("ID del tipo de cliente: ");
        }
        cliente.setIdTipo(tipo);

        return cliente;
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
