package com.frescuratotal.erp.inventory.controller;

import com.frescuratotal.erp.common.LogManager;
import com.frescuratotal.erp.inventory.model.Lot;
import com.frescuratotal.erp.inventory.model.LotDao;
import com.frescuratotal.erp.inventory.model.Product;
import com.frescuratotal.erp.inventory.model.ProductDao;
import com.frescuratotal.erp.inventory.model.Quarantine;
import com.frescuratotal.erp.inventory.model.QuarantineDao;
import com.frescuratotal.erp.inventory.model.Inspection;
import com.frescuratotal.erp.inventory.model.InspectionDao;
import com.frescuratotal.erp.inventory.model.Waste;
import com.frescuratotal.erp.inventory.model.WasteDao;
import com.frescuratotal.erp.inventory.view.LotView;
import com.frescuratotal.erp.inventory.view.PurchaseCsv;
import com.frescuratotal.erp.sales.model.Customer;
import com.frescuratotal.erp.sales.model.CustomerDao;
import com.frescuratotal.erp.suppliers.model.Supplier;
import com.frescuratotal.erp.suppliers.model.SupplierDao;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LotController {

    private LotDao lotDao;
    private LotView lotView;
    private ProductDao productDao;
    private SupplierDao supplierDao;
    private QuarantineDao quarantineDao;
    private InspectionDao inspectionDao;
    private WasteDao wasteDao;
    private CustomerDao customerDao;


    public LotController(LotDao lotDao, LotView lotView, ProductDao productDao, SupplierDao supplierDao,
            QuarantineDao quarantineDao, InspectionDao inspectionDao, WasteDao wasteDao,
            CustomerDao customerDao) {
        this.lotDao = lotDao;
        this.lotView = lotView;
        this.productDao = productDao;
        this.supplierDao = supplierDao;
        this.quarantineDao = quarantineDao;
        this.inspectionDao = inspectionDao;
        this.wasteDao = wasteDao;
        this.customerDao = customerDao;
    }

    public void iniciar(){
        int opcion;
        do {
            opcion = lotView.mostrarMenu();
            switch (opcion) {
                case 1:
                    listarLotes();
                    break;
                case 2:
                    registrarLote();
                    break;
                case 3:
                    buscarLote();
                    break;
                case 4:
                    revisarVencimientos();
                    break;
                case 5:
                    ponerEnCuarentena();
                    break;
                case 6:
                    inspeccionarLote();
                    break;
                case 7:
                    registrarMerma();
                    break;
                case 0:
                    lotView.mostrarMensaje("Regresando al menu principal...");
                    break;
                default:
                    lotView.mostrarMensaje("Opcion invalida.");
            }
        } while (opcion != 0);
    }
    

      private void listarLotes() {
        try {
            List<Lot> lotes = lotDao.obtenerLotes();
            lotView.mostrarLotes(lotes);
        } catch (SQLException e) {
            lotView.mostrarMensaje("Error al listar los lotes: " + e.getMessage());
        }
    }

    private void buscarLote() {
        try {
            String numeroLote = lotView.pedirNumeroLote();
            Lot lote = lotDao.obtenerLotePorNumero(numeroLote);
            if (lote == null) {
                lotView.mostrarMensaje("No se encontro ningun lote con el numero " + numeroLote);
            } else {
                lotView.mostrarDetalleLote(lote);
                List<Customer> clientes = customerDao.obtenerClientesPorLote(lote.getIdLote());
                lotView.mostrarClientesDelLote(clientes);
            }
        } catch (SQLException e) {
            lotView.mostrarMensaje("Error al buscar el lote: " + e.getMessage());
        }
    }

    private void revisarVencimientos() {
        try {
            int actualizados = lotDao.actualizarEstadosPorVencimiento();
            lotView.mostrarMensaje("Revision completada. Se actualizaron " + actualizados + " lotes.");
        } catch (SQLException e) {
            lotView.mostrarMensaje("Error al revisar vencimientos: " + e.getMessage());
        }
    }

    private void ponerEnCuarentena() {
        try {
            String numeroLote = lotView.pedirNumeroLote();
            Lot lote = lotDao.obtenerLotePorNumero(numeroLote);
            if (lote == null) {
                lotView.mostrarMensaje("No se encontro ningun lote con el numero " + numeroLote);
                return;
            }
            if (lote.getIdEstado() == 3 || lote.getIdEstado() == 4) {
                lotView.mostrarMensaje("No se puede poner en cuarentena un lote en estado " + lote.getNombreEstado() + ".");
                return;
            }

            Quarantine cuarentena = lotView.pedirDatosCuarentena();
            cuarentena.setIdLote(lote.getIdLote());

            quarantineDao.registrarCuarentena(cuarentena);
            lotDao.cambiarEstado(lote.getIdLote(), 4);
            lotView.mostrarMensaje("El lote " + numeroLote + " quedo En Cuarentena y bloqueado para la venta.");
        } catch (SQLException e) {
            lotView.mostrarMensaje("Error al poner el lote en cuarentena: " + e.getMessage());
        }
    }

    private void inspeccionarLote() {
         try {
            String numeroLote = lotView.pedirNumeroLote();
            Lot lote = lotDao.obtenerLotePorNumero(numeroLote);
            if (lote == null) {
                lotView.mostrarMensaje("No se encontro ningun lote con el numero " + numeroLote);
                return;
            }
            if (lote.getIdEstado() != 4) {
                lotView.mostrarMensaje("Solo se pueden inspeccionar lotes En Cuarentena. Este lote esta en estado " + lote.getNombreEstado() + ".");
                return;
            }

            Inspection inspeccion = lotView.pedirDatosInspeccion();
            inspeccion.setIdLote(lote.getIdLote());

            int idInspeccion = inspectionDao.registrarInspeccion(inspeccion);

            if (inspeccion.getResultado().equals("Aprobado")) {
                lotDao.cambiarEstado(lote.getIdLote(), 1);
                lotView.mostrarMensaje("Inspeccion aprobada. El lote " + numeroLote + " volvio a estar Disponible.");
            } else {
                Waste merma = new Waste();
                merma.setIdLote(lote.getIdLote());
                merma.setIdInspeccion(idInspeccion);
                merma.setCantidadPerdida(lote.getCantidad());
                merma.setMotivo(inspeccion.getMotivoRechazo());
                merma.setUsuarioRegistro(inspeccion.getNombreInspector());

                wasteDao.registrarMerma(merma);
                lotDao.actualizarCantidad(lote.getIdLote(), 0);
                lotView.mostrarMensaje("Inspeccion rechazada. Se registro una merma de " + lote.getCantidad()
                        + " y el lote " + numeroLote + " quedo con cantidad 0.");
            }

            quarantineDao.resolverCuarentena(lote.getIdLote(), inspeccion.getNotas());
        } catch (SQLException e) {
            lotView.mostrarMensaje("Error al inspeccionar el lote: " + e.getMessage());
        }
    }

    private void registrarMerma() {
        try {
            String numeroLote = lotView.pedirNumeroLote();
            Lot lote = lotDao.obtenerLotePorNumero(numeroLote);
            if (lote == null) {
                lotView.mostrarMensaje("No se encontro ningun lote con el numero " + numeroLote);
                return;
            }
            if (lote.getCantidad() == 0) {
                lotView.mostrarMensaje("El lote " + numeroLote + " ya no tiene cantidad disponible.");
                return;
            }

            lotView.mostrarMensaje("Cantidad disponible: " + lote.getCantidad());

            Waste merma = lotView.pedirDatosMerma();
            merma.setIdLote(lote.getIdLote());

            if (merma.getCantidadPerdida() <= 0 || merma.getCantidadPerdida() > lote.getCantidad()) {
                lotView.mostrarMensaje("Cantidad invalida. Debe ser mayor a 0 y no mayor a " + lote.getCantidad() + ".");
                return;
            }

            wasteDao.registrarMerma(merma);

            double nuevaCantidad = lote.getCantidad() - merma.getCantidadPerdida();
            lotDao.actualizarCantidad(lote.getIdLote(), nuevaCantidad);

            lotView.mostrarMensaje("Merma registrada. Al lote " + numeroLote + " le quedan " + nuevaCantidad + ".");
            LogManager.registrar("INFO", "Merma registrada en el lote " + numeroLote + ", cantidad perdida " + merma.getCantidadPerdida());
        } catch (SQLException e) {
            lotView.mostrarMensaje("Error al registrar la merma: " + e.getMessage());
            LogManager.registrar("ERROR", "Error al registrar la merma: " + e.getMessage());
        }
    }

    private void registrarLote(){
        try{
            List<Product> productos = productDao.obtenerProductos();
            lotView.mostrarProductos(productos);

            List<Supplier> proveedores = supplierDao.obtenerProveedores();
            lotView.mostrarProveedores(proveedores);

            Lot lote = lotView.pedirDatosLote();

            LocalDateTime ahora = LocalDateTime.now();
            String numeroLote = "L-" + ahora.format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
            lote.setNumeroLote(numeroLote);

            lote.setIdEstado(1);

            lotDao.registrarLote(lote);
            lotView.mostrarMensaje("Lote registrado correctamente con numero: " + numeroLote);
            LogManager.registrar("INFO", "Lote registrado: " + numeroLote);
        } catch (SQLException e) {
            lotView.mostrarMensaje("Error al registrar el lote: " + e.getMessage());
            LogManager.registrar("ERROR", "Error al registrar el lote: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------

    public void generarReporte(){
        try{
            List<Lot> lotes = lotDao.obtenerLotesConProveedor();

            if(lotes.isEmpty()){
                lotView.mostrarMensaje("No hay datos de inventario suficientes para generar estadisticas.");
                return;
            }

            Map<String, Double> volumenPorProveedor = lotes.stream()
            .collect(Collectors.groupingBy(
                lote -> lote.getNombreProveedor(),
                Collectors.summingDouble(lote -> lote.getCantidadInicial())
            ));

            String proveedorMayor = volumenPorProveedor.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(entrada -> entrada.getKey())
                    .orElse("Sin datos");

            List<String> proveedoresVencidos = lotes.stream()
                    .filter(lote -> lote.getNombreEstado().equals("Vencido"))
                    .map(lote -> lote.getNombreProveedor())
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());
            lotView.mostrarReporte(proveedorMayor, proveedoresVencidos);
        } catch (SQLException e) {
            lotView.mostrarMensaje("Error al generar el reporte: " + e.getMessage());
        }
    }

    // Esto pertenece al punto 2 del taller de practica
    public void generarReporteCompras() {
        LocalDate hoy = LocalDate.now();
        try {
            List<Lot> compras = lotDao.obtenerComprasDelDia(hoy);

            PurchaseCsv purchaseCsv = new PurchaseCsv();
            String archivo = purchaseCsv.generarCsv(compras, hoy);

            if (compras.isEmpty()) {
                lotView.mostrarMensaje("No hay compras realizadas hoy. Se genero el archivo " + archivo + " solo con la cabecera.");
            } else {
                lotView.mostrarMensaje("Reporte generado: " + archivo + " con " + compras.size() + " compras.");
            }
            LogManager.registrar("INFO", "Reporte diario de compras generado: " + archivo);
        } catch (SQLException e) {
            lotView.mostrarMensaje("Error al consultar las compras: " + e.getMessage());
        } catch (IOException e) {
            lotView.mostrarMensaje("Error al escribir el archivo CSV: " + e.getMessage());
            LogManager.registrar("ERROR", "Error al generar el CSV de compras: " + e.getMessage());
        }
    }
    

}
