package com.frescuratotal.erp.inventory.view;



import com.frescuratotal.erp.inventory.model.Inspection;
import com.frescuratotal.erp.inventory.model.Lot;
import com.frescuratotal.erp.inventory.model.Product;
import com.frescuratotal.erp.inventory.model.Quarantine;
import com.frescuratotal.erp.inventory.model.Waste;
import com.frescuratotal.erp.sales.model.Customer;
import com.frescuratotal.erp.suppliers.model.Supplier;
import java.sql.Date;
import java.util.List;
import java.util.Scanner;

public class LotView {

    private Scanner scanner;

    public LotView(Scanner scanner){
        this.scanner = scanner;
    }

    public int mostrarMenu(){
        System.out.println();
        System.out.println("--- MENU DE LOTES ---");
        System.out.println("1. Listar lotes");
        System.out.println("2. Agregar lotes");
        System.out.println("3. Buscar lote por numero");
        System.out.println("4. Revisar vencimientos");
        System.out.println("5. Poner lote en cuarentena");
        System.out.println("6. Inspeccionar lote en cuarentena");
        System.out.println("7. Registrar merma");
        System.out.println("0. Volver al menu principal");
        return leerNumero("Seleccione una opcion: ");
    }

    public void mostrarLotes(List<Lot> lotes) {
        System.out.println();
        System.out.println("--- LISTA DE LOTES ---");
        if (lotes.isEmpty()) {
            System.out.println("No hay lotes registrados.");
            return;
        }
        for (Lot lote : lotes) {
            System.out.println("Lote: " + lote.getNumeroLote()
                    + " | Producto: " + lote.getNombreProducto()
                    + " | Proveedor: " + lote.getNombreProveedor()
                    + " | Cantidad: " + lote.getCantidad() + " de " + lote.getCantidadInicial()
                    + " | Vence: " + lote.getFechaVencimiento()
                    + " | Ubicacion: " + lote.getPasillo() + "-" + lote.getEstante()
                    + " | Estado: " + lote.getNombreEstado());
        }
    }

    public void mostrarProductos(List<Product> productos) {
        System.out.println();
        System.out.println("--- PRODUCTOS DISPONIBLES ---");
        productos.forEach(producto -> System.out.println(producto.getIdProducto() + ". " + producto.getNombre()));
    }

    public void mostrarProveedores(List<Supplier> proveedores) {
        System.out.println();
        System.out.println("--- PROVEEDORES ---");
        proveedores.forEach(proveedor -> System.out.println(proveedor.getIdProveedor() + ". " + proveedor.getNombre()));
    }


    public Lot pedirDatosLote() {
        System.out.println();
        System.out.println("--- REGISTRAR RECEPCION DE MERCANCIA ---");
        Lot lote = new Lot();

        lote.setIdProducto(leerNumero("ID del producto: "));
        lote.setIdProveedor(leerNumero("ID del proveedor: "));

        lote.setFechaVencimiento(leerFecha("Fecha de vencimiento (AAAA-MM-DD): "));

        double cantidad = leerDecimal("Cantidad recibida: ");
        lote.setCantidadInicial(cantidad);
        lote.setCantidad(cantidad);

        lote.setPrecioCompra(leerDecimal("Precio de compra: "));
        lote.setPrecioVenta(leerDecimal("Precio de venta: "));

        System.out.print("Pasillo: ");
        lote.setPasillo(scanner.nextLine());

        System.out.print("Estante: ");
        lote.setEstante(scanner.nextLine());

        return lote;
    }

    public String pedirNumeroLote(){
        System.out.println();
        System.out.print("Numero de lote: ");
        return scanner.nextLine();
    }

    public void mostrarClientesDelLote(List<Customer> clientes) {
        System.out.println();
        System.out.println("--- CLIENTES QUE RECIBIERON PRODUCTOS DE ESTE LOTE ---");
        if (clientes.isEmpty()) {
            System.out.println("Ningun cliente ha comprado de este lote.");
            return;
        }
        clientes.forEach(cliente -> System.out.println(cliente.getNombre()
                + " | Documento: " + cliente.getDocumento()
                + " | Telefono: " + cliente.getTelefono()));
    }

    public void mostrarDetalleLote(Lot lote) {
        System.out.println();
        System.out.println("--- DETALLE DEL LOTE ---");
        System.out.println("Numero de lote: " + lote.getNumeroLote());
        System.out.println("Producto: " + lote.getNombreProducto());
        System.out.println("Proveedor: " + lote.getNombreProveedor());
        System.out.println("Fecha de ingreso: " + lote.getFechaIngreso());
        System.out.println("Fecha de vencimiento: " + lote.getFechaVencimiento());
        System.out.println("Cantidad recibida: " + lote.getCantidadInicial());
        System.out.println("Cantidad actual: " + lote.getCantidad());
        System.out.println("Precio de compra: " + lote.getPrecioCompra());
        System.out.println("Precio de venta: " + lote.getPrecioVenta());
        System.out.println("Ubicacion: " + lote.getPasillo() + "-" + lote.getEstante());
        System.out.println("Estado: " + lote.getNombreEstado());
    }

    public Quarantine pedirDatosCuarentena() {
        Quarantine cuarentena = new Quarantine();

        System.out.print("Motivo de la cuarentena (ej: empaque danado, olor extrano): ");
        cuarentena.setMotivo(scanner.nextLine());

        System.out.print("Nombre del operario: ");
        cuarentena.setUsuario(scanner.nextLine());

        return cuarentena;
    }

    
    public Inspection pedirDatosInspeccion() {
        Inspection inspeccion = new Inspection();

        System.out.print("Nombre del inspector: ");
        inspeccion.setNombreInspector(scanner.nextLine());

        int eleccion = leerNumero("Resultado (1.Aprobado/2.Rechazado) ");

        if (eleccion == 1) {
            inspeccion.setResultado("Aprobado");
        } else{
            inspeccion.setResultado("Rechazado");
            System.out.print("Motivo del rechazo: ");
            inspeccion.setMotivoRechazo(scanner.nextLine());
        }

        System.out.print("Notas: ");
        inspeccion.setNotas(scanner.nextLine());

        return inspeccion;
    }

     public Waste pedirDatosMerma() {
        Waste merma = new Waste();

        double perdida = leerDecimal("Cantidad perdida: ");
        merma.setCantidadPerdida(perdida);

        System.out.print("Motivo (ej: vencido, dañado): ");
        merma.setMotivo(scanner.nextLine());

        System.out.print("Nombre de quien registra: ");
        merma.setUsuarioRegistro(scanner.nextLine());

        return merma;
    }


     public void mostrarMensaje(String mensaje) {
        System.out.println(mensaje);
    }

    // ------------------------------------------------------------------

    public void mostrarReporte(String proveedorMayor, List<String> proveedoresVencidos) {
        System.out.println();
        System.out.println("* Reporte de Rendimiento de Proveedores *");
        System.out.println();
        System.out.println(">> Proveedor con Mayor Volumen de Suministro:");
        System.out.println(proveedorMayor);
        System.out.println();
        System.out.println(">> Proveedores con Lotes Vencidos Registrados:");
        if (proveedoresVencidos.isEmpty()) {
            System.out.println("Ningun proveedor tiene lotes vencidos.");
        } else {
            proveedoresVencidos.forEach(nombre -> System.out.println("- " + nombre));
        }
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
                System.out.println("Debe ingresar un valor numerico (ej: 2500.50).");
            }
        }
    }

    private Date leerFecha(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String texto = scanner.nextLine();
            try {
                return Date.valueOf(texto);
            } catch (IllegalArgumentException e) {
                System.out.println("Fecha invalida. Use el formato AAAA-MM-DD (ej: 2026-11-20).");
            }
        }
    }

}
