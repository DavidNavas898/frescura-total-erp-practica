package com.frescuratotal.erp.sales.controller;

import com.frescuratotal.erp.common.LogManager;
import com.frescuratotal.erp.inventory.model.Lot;
import com.frescuratotal.erp.inventory.model.LotDao;
import com.frescuratotal.erp.inventory.model.Product;
import com.frescuratotal.erp.inventory.model.ProductDao;
import com.frescuratotal.erp.sales.model.Customer;
import com.frescuratotal.erp.sales.model.CustomerCache;
import com.frescuratotal.erp.sales.model.CustomerDao;
import com.frescuratotal.erp.sales.model.DeliveryCalculator;
import com.frescuratotal.erp.sales.model.SalesRecord;
import com.frescuratotal.erp.sales.model.Invoice;
import com.frescuratotal.erp.sales.model.InvoiceDao;
import com.frescuratotal.erp.sales.model.Sale;
import com.frescuratotal.erp.sales.model.SaleDao;
import com.frescuratotal.erp.sales.model.SaleDetail;
import com.frescuratotal.erp.sales.view.InvoicePdf;
import com.frescuratotal.erp.sales.view.SaleView;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class SaleController {

    private static final double DESCUENTO_PROXIMO_A_VENCER = 0.50;
    private static final double TASA_IVA = 0.19;
    // Esto pertenece al punto 5 del taller de practica
    private static final String ESTADO_CONFIRMADA = "Entregada";
    private static final String ESTADO_DEVUELTA = "Cancelada";

    private SaleDao saleDao;
    private SaleView saleView;
    private CustomerDao customerDao;
    private ProductDao productDao;
    private LotDao lotDao;
    private InvoiceDao invoiceDao;

    public SaleController(SaleDao saleDao, SaleView saleView, CustomerDao customerDao, ProductDao productDao,
            LotDao lotDao, InvoiceDao invoiceDao) {
        this.saleDao = saleDao;
        this.saleView = saleView;
        this.customerDao = customerDao;
        this.productDao = productDao;
        this.lotDao = lotDao;
        this.invoiceDao = invoiceDao;
    }

    public void iniciar() {
        int opcion;
        do {
            opcion = saleView.mostrarMenu();
            switch (opcion) {
                case 1:
                    nuevaVenta();
                    break;
                case 2:
                    enviarAPreparacion();
                    break;
                case 0:
                    saleView.mostrarMensaje("Regresando al menu principal...");
                    break;
                default:
                    saleView.mostrarMensaje("Opcion invalida.");
            }
        } while (opcion != 0);
    }

    private void nuevaVenta() {
        try {
            List<Customer> clientes = customerDao.obtenerClientes();
            if (clientes.isEmpty()) {
                saleView.mostrarMensaje("No hay clientes registrados. Primero registre un cliente.");
                return;
            }
            saleView.mostrarClientes(clientes);
            int idCliente = saleView.pedirIdCliente();

            // Esto pertenece al punto 1 del taller de practica
            CustomerCache cache = CustomerCache.getInstance();
            if (cache.estaEnCache(idCliente)) {
                saleView.mostrarMensaje("(Cliente obtenido de la cache, sin consultar la base de datos)");
            }
            Customer clienteElegido = cache.obtenerCliente(idCliente);
            if (clienteElegido == null) {
                saleView.mostrarMensaje("El cliente no existe. Venta cancelada.");
                return;
            }

            Sale venta = new Sale();
            venta.setIdCliente(idCliente);

            List<Product> productos = productDao.obtenerProductos();
            int idProducto;
            do {
                saleView.mostrarProductos(productos);
                idProducto = saleView.pedirIdProducto();
                if (idProducto != 0) {
                    agregarProducto(venta, idProducto);
                }
            } while (idProducto != 0);

            if (venta.getDetalles().isEmpty()) {
                saleView.mostrarMensaje("El pedido esta vacio. Venta cancelada.");
                return;
            }

            calcularTotales(venta);

            // Esto pertenece al punto 3 del taller de practica
            String modalidad = saleView.pedirModalidadEntrega();
            DeliveryCalculator calculadora = new DeliveryCalculator();
            double totalConEntrega = calculadora.calcularTotalConEntrega(venta, modalidad);
            saleView.mostrarMensaje("Entrega " + modalidad + ": total " + venta.getMontoTotal() + " -> " + totalConEntrega);
            venta.setMontoTotal(totalConEntrega);

            saleView.mostrarResumen(venta);

            if (!saleView.confirmar("Confirmar la venta?")) {
                saleView.mostrarMensaje("Venta cancelada.");
                return;
            }

            // 4. Guardar todo en una transaccion
            LocalDateTime ahora = LocalDateTime.now();
            venta.setNumeroVenta("V-" + ahora.format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")));
            venta.setIdEstado(1);
            venta.setUsuarioRegistro(saleView.pedirVendedor());

            int idVenta = saleDao.registrarVenta(venta);
            saleView.mostrarMensaje("Venta " + venta.getNumeroVenta() + " registrada con exito (ID " + idVenta + ").");
            LogManager.registrar("INFO", "Venta registrada: " + venta.getNumeroVenta() + ", total " + venta.getMontoTotal());

            Invoice factura = new Invoice();
            factura.setIdVenta(idVenta);
            factura.setNumeroFactura("FE-" + ahora.format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")));

            factura.setSubtotal(venta.getSubtotal());
            factura.setTotalDescuento(venta.getTotalDescuento());
            factura.setMontoImpuesto(venta.getTotalImpuesto());
            factura.setMontoTotal(venta.getMontoTotal());

            String datosCufe = factura.getNumeroFactura() + ahora + venta.getSubtotal()
                    + venta.getTotalImpuesto() + venta.getMontoTotal() + "900123456-7";
            factura.setCufe(generarCufe(datosCufe));
            factura.setEnlaceQr("https://catalogo-vpfe.dian.gov.co/document/searchqr?documentkey=" + factura.getCufe());

            factura.setFechaEmision(Timestamp.valueOf(LocalDateTime.now()));
            invoiceDao.registrarFactura(factura);
            saleView.mostrarMensaje("Factura " + factura.getNumeroFactura() + " generada.");
            saleView.mostrarMensaje("CUFE: " + factura.getCufe());

            try {
                InvoicePdf invoicePdf = new InvoicePdf();
                String ruta = invoicePdf.generarFactura(factura, venta, clienteElegido);
                saleView.mostrarMensaje("PDF de la factura guardado en: " + ruta);
            } catch (Exception e) {
                saleView.mostrarMensaje("La venta se guardo, pero hubo un error al generar el PDF: " + e.getMessage());
                LogManager.registrar("ERROR", "Error al generar el PDF: " + e.getMessage());
            }
        } catch (SQLException e) {
            saleView.mostrarMensaje("Error al registrar la venta: " + e.getMessage());
            LogManager.registrar("ERROR", "Error al registrar la venta: " + e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            saleView.mostrarMensaje("Error al generar el CUFE: " + e.getMessage());
        }
    }

    private void enviarAPreparacion() {
        try {
            List<Sale> ventas = saleDao.obtenerVentasPorEstado(1);
            saleView.mostrarVentas(ventas);
            if (ventas.isEmpty()) {
                return;
            }
            int idVenta = saleView.pedirIdVenta();

            boolean ventaExiste = false;
            for (Sale venta : ventas) {
                if (venta.getIdVenta() == idVenta) {
                    ventaExiste = true;
                }
            }
            if (!ventaExiste) {
                saleView.mostrarMensaje("Esa venta no esta en estado Registrada.");
                return;
            }

            saleDao.cambiarEstado(idVenta, 2);
            saleView.mostrarMensaje("Venta enviada a preparacion. Ya aparece en el panel de despachos de logistica.");
        } catch (SQLException e) {
            saleView.mostrarMensaje("Error al enviar la venta a preparacion: " + e.getMessage());
        }
    }

    // Esto pertenece al punto 5 del taller de practica
    public void generarEstadisticas() {
        try {
            List<SalesRecord> registros = saleDao.obtenerRegistrosComerciales();

            if (registros.isEmpty()) {
                saleView.mostrarMensaje("No existen datos suficientes para generar estadísticas comerciales.");
                return;
            }

            Map<String, Double> unidadesPorCategoria = registros.stream()
                    .filter(registro -> registro.getEstadoVenta().equals(ESTADO_CONFIRMADA))
                    .collect(Collectors.groupingBy(
                            registro -> registro.getNombreCategoria(),
                            Collectors.summingDouble(registro -> registro.getCantidad())
                    ));

            String categoriaMayor = unidadesPorCategoria.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(entrada -> entrada.getKey())
                    .orElse("Sin ventas confirmadas");

            double unidades = 0;
            if (unidadesPorCategoria.containsKey(categoriaMayor)) {
                unidades = unidadesPorCategoria.get(categoriaMayor);
            }

            List<String> clientesConDevoluciones = registros.stream()
                    .filter(registro -> registro.getEstadoVenta().equals(ESTADO_DEVUELTA))
                    .map(registro -> registro.getNombreCliente())
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());

            saleView.mostrarEstadisticas(categoriaMayor, unidades, clientesConDevoluciones);
        } catch (SQLException e) {
            saleView.mostrarMensaje("Error al generar las estadisticas: " + e.getMessage());
        }
    }

    private void agregarProducto(Sale venta, int idProducto) throws SQLException {
        List<Lot> lotes = lotDao.obtenerLotesDisponiblesPorProducto(idProducto);
        if (lotes.isEmpty()) {
            saleView.mostrarMensaje("No hay stock disponible de ese producto.");
            return;
        }

        double stockTotal = 0;
        for (Lot lote : lotes) {
            lote.setCantidad(lote.getCantidad() - cantidadYaEnPedido(venta, lote.getIdLote()));
            stockTotal = stockTotal + lote.getCantidad();
        }

        saleView.mostrarLotesDisponibles(lotes);
        double cantidad = saleView.pedirCantidad();
        if (cantidad <= 0) {
            saleView.mostrarMensaje("La cantidad debe ser mayor que 0.");
            return;
        }
        if (cantidad > stockTotal) {
            saleView.mostrarMensaje("Stock insuficiente. Solo hay " + stockTotal + " disponibles.");
            return;
        }

       
        double restante = cantidad;
        for (Lot lote : lotes) {
            if (restante <= 0) {
                break;
            }
            if (lote.getCantidad() <= 0) {
                continue;
            }

            double tomar;
            if (restante <= lote.getCantidad()) {
                tomar = restante;
            } else {
                tomar = lote.getCantidad();
            }

            venta.agregarDetalle(crearDetalle(lote, tomar));
            restante = restante - tomar;
        }
        saleView.mostrarMensaje("Producto agregado al pedido.");
    }

    private SaleDetail crearDetalle(Lot lote, double cantidad) {
        SaleDetail detalle = new SaleDetail();
        detalle.setIdLote(lote.getIdLote());
        detalle.setNumeroLote(lote.getNumeroLote());
        detalle.setNombreProducto(lote.getNombreProducto());
        detalle.setCantidadVendida(cantidad);
        detalle.setPrecioUnitario(lote.getPrecioVenta());

        double subtotal = redondear(cantidad * lote.getPrecioVenta());

        double descuento = 0;
        if (lote.getIdEstado() == 2) {
            descuento = DESCUENTO_PROXIMO_A_VENCER;
        }
        double montoDescuento = redondear(subtotal * descuento);
        double montoImpuesto = redondear((subtotal - montoDescuento) * TASA_IVA);
        double totalLinea = redondear(subtotal - montoDescuento + montoImpuesto);

        detalle.setSubtotal(subtotal);
        detalle.setDescuentoAplicado(descuento);
        detalle.setMontoDescuento(montoDescuento);
        detalle.setTasaImpuesto(TASA_IVA);
        detalle.setMontoImpuesto(montoImpuesto);
        detalle.setTotalLinea(totalLinea);
        return detalle;
    }

    private double cantidadYaEnPedido(Sale venta, int idLote) {
        return venta.getDetalles().stream()
                .filter(detalle -> detalle.getIdLote() == idLote)
                .mapToDouble(detalle -> detalle.getCantidadVendida())
                .sum();
    }

    private void calcularTotales(Sale venta) {
        double subtotal = 0;
        double totalDescuento = 0;
        double totalImpuesto = 0;
        double montoTotal = 0;
        for (SaleDetail detalle : venta.getDetalles()) {
            subtotal = subtotal + detalle.getSubtotal();
            totalDescuento = totalDescuento + detalle.getMontoDescuento();
            totalImpuesto = totalImpuesto + detalle.getMontoImpuesto();
            montoTotal = montoTotal + detalle.getTotalLinea();
        }
        venta.setSubtotal(redondear(subtotal));
        venta.setTotalDescuento(redondear(totalDescuento));
        venta.setTotalImpuesto(redondear(totalImpuesto));
        venta.setMontoTotal(redondear(montoTotal));
    }

    private double redondear(double valor) {
        return Math.round(valor * 100) / 100.0;
    }

    private String generarCufe(String datos) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-384");
        byte[] hash = digest.digest(datos.getBytes());
        return HexFormat.of().formatHex(hash).toUpperCase();
    }
}
