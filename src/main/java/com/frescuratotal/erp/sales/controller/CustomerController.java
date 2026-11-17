package com.frescuratotal.erp.sales.controller;

import com.frescuratotal.erp.common.LogManager;
import com.frescuratotal.erp.sales.model.Customer;
import com.frescuratotal.erp.sales.model.CustomerCache;
import com.frescuratotal.erp.sales.model.CustomerDao;
import com.frescuratotal.erp.sales.view.CustomerView;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CustomerController {

    private CustomerDao customerDao;
    private CustomerView customerView;

    public CustomerController(CustomerDao customerDao, CustomerView customerView) {
        this.customerDao = customerDao;
        this.customerView = customerView;
    }

    public void iniciar() {
        int opcion;
        do {
            opcion = customerView.mostrarMenu();
            switch (opcion) {
                case 1:
                    listarClientes();
                    break;
                case 2:
                    registrarCliente();
                    break;
                case 3:
                    buscarClientePorId();
                    break;
                case 4:
                    importarClientes();
                    break;
                case 0:
                    customerView.mostrarMensaje("Regresando al menu principal...");
                    break;
                default:
                    customerView.mostrarMensaje("Opcion invalida.");
            }
        } while (opcion != 0);
    }

    private void listarClientes() {
        try {
            List<Customer> clientes = customerDao.obtenerClientes();
            customerView.mostrarClientes(clientes);
        } catch (SQLException e) {
            customerView.mostrarMensaje("Error al listar clientes: " + e.getMessage());
        }
    }

    private void registrarCliente() {
        Customer cliente = customerView.pedirDatosCliente();

        if (cliente.getNombre().isEmpty() || cliente.getDocumento().isEmpty()) {
            customerView.mostrarMensaje("El nombre y el documento son obligatorios. Cliente no registrado.");
            return;
        }

        try {
            customerDao.registrarCliente(cliente);
            customerView.mostrarMensaje("Cliente registrado exitosamente.");
            LogManager.registrar("INFO", "Cliente registrado: " + cliente.getNombre());
        } catch (SQLException e) {
            customerView.mostrarMensaje("Error al registrar cliente: " + e.getMessage());
            LogManager.registrar("ERROR", "Error al registrar cliente: " + e.getMessage());
        }
    }

    private void buscarClientePorId() {
        int idCliente = customerView.pedirIdCliente();
        CustomerCache cache = CustomerCache.getInstance();
        try {
            if (cache.estaEnCache(idCliente)) {
                customerView.mostrarMensaje("(Cliente obtenido de la cache, sin consultar la base de datos)");
            } else {
                customerView.mostrarMensaje("(Cliente consultado en la base de datos)");
            }

            Customer cliente = cache.obtenerCliente(idCliente);
            if (cliente == null) {
                customerView.mostrarMensaje("El cliente con ID " + idCliente + " no fue encontrado.");
            } else {
                customerView.mostrarCliente(cliente);
            }
        } catch (SQLException e) {
            customerView.mostrarMensaje("Error al buscar el cliente: " + e.getMessage());
        }
    }

    private void importarClientes() {
        File archivo = new File("clientes_importar.txt");
        if (!archivo.exists()) {
            customerView.mostrarMensaje("Error: No se encontró el archivo clientes_importar.txt.");
            return;
        }

        List<Customer> clientesNuevos = new ArrayList<>();
        List<String> documentosDelArchivo = new ArrayList<>();
        boolean hayErrores = false;

        try (BufferedReader lector = new BufferedReader(new FileReader(archivo))) {
            String cabecera = lector.readLine();
            if (cabecera == null || !cabecera.equals("nombre;documento;correo;telefono;tipo_cliente")) {
                customerView.mostrarMensaje("La cabecera del archivo no es la esperada.");
                hayErrores = true;
            }

            String linea = lector.readLine();
            int numeroLinea = 2;
            while (linea != null && !hayErrores) {
                if (!linea.trim().isEmpty()) {
                    String[] campos = linea.split(";", -1);

                    if (campos.length != 5) {
                        customerView.mostrarMensaje("Linea " + numeroLinea + ": debe tener 5 campos.");
                        hayErrores = true;
                    } else {
                        String nombre = campos[0].trim();
                        String documento = campos[1].trim();
                        String correo = campos[2].trim();
                        String telefono = campos[3].trim();
                        String tipo = campos[4].trim();

                        if (nombre.isEmpty() || documento.isEmpty() || correo.isEmpty()
                                || telefono.isEmpty() || tipo.isEmpty()) {
                            customerView.mostrarMensaje("Linea " + numeroLinea + ": hay campos vacios.");
                            hayErrores = true;
                        } else if (!esCorreoValido(correo)) {
                            customerView.mostrarMensaje("Linea " + numeroLinea + ": el correo no es valido.");
                            hayErrores = true;
                        } else if (!tipo.equals("MAYORISTA") && !tipo.equals("MINORISTA")) {
                            customerView.mostrarMensaje("Linea " + numeroLinea + ": el tipo debe ser MAYORISTA o MINORISTA.");
                            hayErrores = true;
                        } else if (documentosDelArchivo.contains(documento)) {
                            customerView.mostrarMensaje("Linea " + numeroLinea + ": el documento esta repetido en el archivo.");
                            hayErrores = true;
                        } else if (customerDao.existeDocumento(documento)) {
                            customerView.mostrarMensaje("Linea " + numeroLinea + ": el documento ya existe en la base de datos.");
                            hayErrores = true;
                        } else {
                            Customer cliente = new Customer();
                            cliente.setNombre(nombre);
                            cliente.setDocumento(documento);
                            cliente.setEmail(correo);
                            cliente.setTelefono(telefono);
                            if (tipo.equals("MAYORISTA")) {
                                cliente.setIdTipo(1);
                            } else {
                                cliente.setIdTipo(2);
                            }
                            clientesNuevos.add(cliente);
                            documentosDelArchivo.add(documento);
                        }
                    }
                }
                linea = lector.readLine();
                numeroLinea++;
            }

            if (hayErrores) {
                customerView.mostrarMensaje("Error: Se encontraron registros inválidos. Importación cancelada.");
                return;
            }

            int total = customerDao.registrarClientes(clientesNuevos);
            customerView.mostrarMensaje("Importación completada. Total de clientes registrados: " + total);
            LogManager.registrar("INFO", "Clientes importados desde archivo: " + total);
        } catch (IOException e) {
            customerView.mostrarMensaje("Error al leer el archivo: " + e.getMessage());
        } catch (SQLException e) {
            customerView.mostrarMensaje("Error: Se encontraron registros inválidos. Importación cancelada.");
            LogManager.registrar("ERROR", "Error al importar clientes: " + e.getMessage());
        }
    }

    private boolean esCorreoValido(String correo) {
        if (correo.contains(" ")) {
            return false;
        }
        int posicionArroba = correo.indexOf("@");
        int posicionPunto = correo.lastIndexOf(".");
        if (posicionArroba <= 0) {
            return false;
        }
        if (posicionPunto < posicionArroba + 2) {
            return false;
        }
        if (posicionPunto == correo.length() - 1) {
            return false;
        }
        return true;
    }
}
