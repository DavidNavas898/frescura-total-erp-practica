package com.frescuratotal.erp.sales.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.frescuratotal.erp.common.DatabaseConnection;

public class SaleDao {

    public int registrarVenta(Sale venta) throws SQLException {
        String sqlVenta = "INSERT INTO ventas (numero_venta, id_cliente, subtotal, total_descuento, total_impuesto, "
                + "monto_total, id_estado, usuario_registro) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlDetalle = "INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, "
                + "descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlLote = "UPDATE lotes_inventario SET cantidad = cantidad - ? WHERE id_lote = ? AND cantidad >= ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int idVenta = -1;
                try (PreparedStatement stmt = conn.prepareStatement(sqlVenta, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, venta.getNumeroVenta());
                    stmt.setInt(2, venta.getIdCliente());
                    stmt.setDouble(3, venta.getSubtotal());
                    stmt.setDouble(4, venta.getTotalDescuento());
                    stmt.setDouble(5, venta.getTotalImpuesto());
                    stmt.setDouble(6, venta.getMontoTotal());
                    stmt.setInt(7, venta.getIdEstado());
                    stmt.setString(8, venta.getUsuarioRegistro());
                    stmt.executeUpdate();
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            idVenta = rs.getInt(1);
                        }
                    }
                }

                for (SaleDetail detalle : venta.getDetalles()) {
                    try (PreparedStatement stmt = conn.prepareStatement(sqlDetalle)) {
                        stmt.setInt(1, idVenta);
                        stmt.setInt(2, detalle.getIdLote());
                        stmt.setDouble(3, detalle.getCantidadVendida());
                        stmt.setDouble(4, detalle.getPrecioUnitario());
                        stmt.setDouble(5, detalle.getDescuentoAplicado());
                        stmt.setDouble(6, detalle.getMontoDescuento());
                        stmt.setDouble(7, detalle.getTasaImpuesto());
                        stmt.setDouble(8, detalle.getMontoImpuesto());
                        stmt.setDouble(9, detalle.getSubtotal());
                        stmt.setDouble(10, detalle.getTotalLinea());
                        stmt.executeUpdate();
                    }

                    try (PreparedStatement stmt = conn.prepareStatement(sqlLote)) {
                        stmt.setDouble(1, detalle.getCantidadVendida());
                        stmt.setInt(2, detalle.getIdLote());
                        stmt.setDouble(3, detalle.getCantidadVendida());
                        int filas = stmt.executeUpdate();
                        if (filas == 0) {
                            throw new SQLException("Stock insuficiente en el lote " + detalle.getNumeroLote());
                        }
                    }
                }

                conn.commit();
                return idVenta;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public List<Sale> obtenerVentasPorEstado(int idEstado) throws SQLException {
        List<Sale> ventas = new ArrayList<>();
        String sql = "SELECT v.*, c.nombre AS nombre_cliente, e.nombre AS nombre_estado "
                + "FROM ventas v "
                + "JOIN clientes c ON v.id_cliente = c.id_cliente "
                + "JOIN estados_venta e ON v.id_estado = e.id_estado "
                + "WHERE v.id_estado = ? "
                + "ORDER BY v.fecha_venta";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idEstado);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Sale venta = new Sale();
                venta.setIdVenta(rs.getInt("id_venta"));
                venta.setNumeroVenta(rs.getString("numero_venta"));
                venta.setIdCliente(rs.getInt("id_cliente"));
                venta.setFechaVenta(rs.getTimestamp("fecha_venta"));
                venta.setSubtotal(rs.getDouble("subtotal"));
                venta.setTotalDescuento(rs.getDouble("total_descuento"));
                venta.setTotalImpuesto(rs.getDouble("total_impuesto"));
                venta.setMontoTotal(rs.getDouble("monto_total"));
                venta.setIdEstado(rs.getInt("id_estado"));
                venta.setUsuarioRegistro(rs.getString("usuario_registro"));
                venta.setNombreCliente(rs.getString("nombre_cliente"));
                venta.setNombreEstado(rs.getString("nombre_estado"));
                ventas.add(venta);
            }
        }
        return ventas;
    }

    public List<SalesRecord> obtenerRegistrosComerciales() throws SQLException {
        List<SalesRecord> registros = new ArrayList<>();
        String sql = "SELECT v.id_venta, d.cantidad_vendida, cat.nombre AS nombre_categoria, "
                + "c.nombre AS nombre_cliente, e.nombre AS estado_venta "
                + "FROM ventas v "
                + "JOIN detalles_venta d ON d.id_venta = v.id_venta "
                + "JOIN lotes_inventario l ON d.id_lote = l.id_lote "
                + "JOIN productos p ON l.id_producto = p.id_producto "
                + "JOIN categorias_producto cat ON p.id_categoria = cat.id_categoria "
                + "JOIN clientes c ON v.id_cliente = c.id_cliente "
                + "JOIN estados_venta e ON v.id_estado = e.id_estado";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                SalesRecord registro = new SalesRecord();
                registro.setIdVenta(rs.getInt("id_venta"));
                registro.setCantidad(rs.getDouble("cantidad_vendida"));
                registro.setNombreCategoria(rs.getString("nombre_categoria"));
                registro.setNombreCliente(rs.getString("nombre_cliente"));
                registro.setEstadoVenta(rs.getString("estado_venta"));
                registros.add(registro);
            }
        }
        return registros;
    }

    public void cambiarEstado(int idVenta, int idEstado) throws SQLException {
        String sql = "UPDATE ventas SET id_estado = ? WHERE id_venta = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idEstado);
            stmt.setInt(2, idVenta);
            stmt.executeUpdate();
        }
    }
}
