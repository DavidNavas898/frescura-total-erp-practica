package com.frescuratotal.erp.inventory.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.xml.crypto.Data;

import com.frescuratotal.erp.common.DatabaseConnection;

public class LotDao {
    public List<Lot> obtenerLotes() throws SQLException{
        List<Lot> lotes = new ArrayList<>();
        String sql = "SELECT l.*,\r\n" + //
                        "       p.nombre  AS nombre_producto,\r\n" + //
                        "       pr.nombre AS nombre_proveedor,\r\n" + //
                        "       e.nombre  AS nombre_estado\r\n" + //
                        "FROM lotes_inventario l\r\n" + //
                        "JOIN productos p     ON l.id_producto  = p.id_producto\r\n" + //
                        "JOIN proveedores pr  ON l.id_proveedor = pr.id_proveedor\r\n" + //
                        "JOIN estados_lote e  ON l.id_estado    = e.id_estado\r\n" + //
                        "ORDER BY l.fecha_vencimiento\r\n" + //
                        "";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()){
            while (rs.next()){
                Lot lote = new Lot();
                lote.setIdLote(rs.getInt("id_lote"));
                lote.setNumeroLote(rs.getString("numero_lote"));
                lote.setIdProducto(rs.getInt("id_producto"));
                lote.setIdProveedor(rs.getInt("id_proveedor"));
                lote.setFechaIngreso(rs.getDate("fecha_ingreso"));
                lote.setFechaVencimiento(rs.getDate("fecha_vencimiento"));
                lote.setCantidadInicial(rs.getDouble("cantidad_inicial"));
                lote.setCantidad(rs.getDouble("cantidad"));
                lote.setPrecioCompra(rs.getDouble("precio_compra"));
                lote.setPrecioVenta(rs.getDouble("precio_venta"));
                lote.setPasillo(rs.getString("pasillo"));
                lote.setEstante(rs.getString("estante"));
                lote.setIdEstado(rs.getInt("id_estado"));
                lote.setNombreProducto(rs.getString("nombre_producto"));
                lote.setNombreProveedor(rs.getString("nombre_proveedor"));
                lote.setNombreEstado(rs.getString("nombre_estado"));
                lotes.add(lote);
            }
        }
        return lotes;
    }


    public Lot obtenerLotePorNumero(String numeroLote) throws SQLException{
        String sql = "SELECT l.*,\r\n" + //
                        "       p.nombre  AS nombre_producto,\r\n" + //
                        "       pr.nombre AS nombre_proveedor,\r\n" + //
                        "       e.nombre  AS nombre_estado\r\n" + //
                        "FROM lotes_inventario l\r\n" + //
                        "JOIN productos p     ON l.id_producto  = p.id_producto\r\n" + //
                        "JOIN proveedores pr  ON l.id_proveedor = pr.id_proveedor\r\n" + //
                        "JOIN estados_lote e  ON l.id_estado    = e.id_estado\r\n" + //
                        "WHERE l.numero_lote = ?\r\n" + //
                        "";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setString(1, numeroLote);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()){
                Lot lote = new Lot();
                lote.setIdLote(rs.getInt("id_lote"));
                lote.setNumeroLote(rs.getString("numero_lote"));
                lote.setIdProducto(rs.getInt("id_producto"));
                lote.setIdProveedor(rs.getInt("id_proveedor"));
                lote.setFechaIngreso(rs.getDate("fecha_ingreso"));
                lote.setFechaVencimiento(rs.getDate("fecha_vencimiento"));
                lote.setCantidadInicial(rs.getDouble("cantidad_inicial"));
                lote.setCantidad(rs.getDouble("cantidad"));
                lote.setPrecioCompra(rs.getDouble("precio_compra"));
                lote.setPrecioVenta(rs.getDouble("precio_venta"));
                lote.setPasillo(rs.getString("pasillo"));
                lote.setEstante(rs.getString("estante"));
                lote.setIdEstado(rs.getInt("id_estado"));
                lote.setNombreProducto(rs.getString("nombre_producto"));
                lote.setNombreProveedor(rs.getString("nombre_proveedor"));
                lote.setNombreEstado(rs.getString("nombre_estado"));
                return lote;
            }
        }
        return null;
    }

    public void registrarLote(Lot lote) throws SQLException{
        String sql = "INSERT INTO lotes_inventario(numero_lote, id_producto, id_proveedor,fecha_vencimiento, cantidad_inicial, cantidad, precio_compra, precio_venta, pasillo, estante, id_estado) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setString(1, lote.getNumeroLote());
            stmt.setInt(2,lote.getIdProducto());
            stmt.setInt(3,lote.getIdProveedor());
            stmt.setDate(4, lote.getFechaVencimiento());
            stmt.setDouble(5, lote.getCantidadInicial());
            stmt.setDouble(6, lote.getCantidad());
            stmt.setDouble(7, lote.getPrecioCompra());
            stmt.setDouble(8, lote.getPrecioVenta());
            stmt.setString(9,lote.getPasillo());
            stmt.setString(10,lote.getEstante());
            stmt.setInt(11, lote.getIdEstado());
            stmt.executeUpdate();
        }
    }


    public int actualizarEstadosPorVencimiento()throws SQLException{
        String sqlVencidos = "UPDATE lotes_inventario SET id_estado = 3 "
                + "WHERE fecha_vencimiento < CURDATE() "
                + "AND id_estado IN (1, 2)";
        String sqlProximos = "UPDATE lotes_inventario SET id_estado = 2 "
                + "WHERE fecha_vencimiento <= DATE_ADD(CURDATE(), INTERVAL 7 DAY) "
                + "AND id_estado = 1";

        int actualizados = 0;
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmtVencidos = conn.prepareStatement(sqlVencidos);
                PreparedStatement stmtProximos = conn.prepareStatement(sqlProximos)){
            actualizados = actualizados + stmtVencidos.executeUpdate();
            actualizados = actualizados + stmtProximos.executeUpdate();
        }
        return actualizados;
    }

    public void cambiarEstado(int idLote, int idEstado) throws SQLException {
        String sql = "UPDATE lotes_inventario SET id_estado = ? WHERE id_lote = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idEstado);
            stmt.setInt(2, idLote);
            stmt.executeUpdate();
        }
    }

    public void actualizarCantidad(int idLote, double cantidad) throws SQLException {
        String sql = "UPDATE lotes_inventario SET cantidad = ? WHERE id_lote = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, cantidad);
            stmt.setInt(2, idLote);
            stmt.executeUpdate();
        }
    }

    public List<Lot> obtenerLotesPorProveedor(int idProveedor) throws SQLException {
        List<Lot> lotes = new ArrayList<>();
        String sql = "SELECT l.*, p.nombre AS nombre_producto, pr.nombre AS nombre_proveedor, e.nombre AS nombre_estado "
                + "FROM lotes_inventario l "
                + "JOIN productos p ON l.id_producto = p.id_producto "
                + "JOIN proveedores pr ON l.id_proveedor = pr.id_proveedor "
                + "JOIN estados_lote e ON l.id_estado = e.id_estado "
                + "WHERE l.id_proveedor = ? "
                + "ORDER BY l.fecha_ingreso DESC";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idProveedor);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Lot lote = new Lot();
                lote.setIdLote(rs.getInt("id_lote"));
                lote.setNumeroLote(rs.getString("numero_lote"));
                lote.setIdProducto(rs.getInt("id_producto"));
                lote.setIdProveedor(rs.getInt("id_proveedor"));
                lote.setFechaIngreso(rs.getDate("fecha_ingreso"));
                lote.setFechaVencimiento(rs.getDate("fecha_vencimiento"));
                lote.setCantidadInicial(rs.getDouble("cantidad_inicial"));
                lote.setCantidad(rs.getDouble("cantidad"));
                lote.setPrecioCompra(rs.getDouble("precio_compra"));
                lote.setPrecioVenta(rs.getDouble("precio_venta"));
                lote.setPasillo(rs.getString("pasillo"));
                lote.setEstante(rs.getString("estante"));
                lote.setIdEstado(rs.getInt("id_estado"));
                lote.setNombreProducto(rs.getString("nombre_producto"));
                lote.setNombreProveedor(rs.getString("nombre_proveedor"));
                lote.setNombreEstado(rs.getString("nombre_estado"));
                lotes.add(lote);
            }
        }
        return lotes;
    }

    public List<Lot> obtenerLotesDisponiblesPorProducto(int idProducto) throws SQLException {
        List<Lot> lotes = new ArrayList<>();
        String sql = "SELECT l.*, p.nombre AS nombre_producto, pr.nombre AS nombre_proveedor, e.nombre AS nombre_estado "
                + "FROM lotes_inventario l "
                + "JOIN productos p ON l.id_producto = p.id_producto "
                + "JOIN proveedores pr ON l.id_proveedor = pr.id_proveedor "
                + "JOIN estados_lote e ON l.id_estado = e.id_estado "
                + "WHERE l.id_producto = ? "
                + "AND l.id_estado IN (1, 2) "
                + "AND l.cantidad > 0 "
                + "ORDER BY l.fecha_vencimiento ASC";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idProducto);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Lot lote = new Lot();
                lote.setIdLote(rs.getInt("id_lote"));
                lote.setNumeroLote(rs.getString("numero_lote"));
                lote.setIdProducto(rs.getInt("id_producto"));
                lote.setIdProveedor(rs.getInt("id_proveedor"));
                lote.setFechaIngreso(rs.getDate("fecha_ingreso"));
                lote.setFechaVencimiento(rs.getDate("fecha_vencimiento"));
                lote.setCantidadInicial(rs.getDouble("cantidad_inicial"));
                lote.setCantidad(rs.getDouble("cantidad"));
                lote.setPrecioCompra(rs.getDouble("precio_compra"));
                lote.setPrecioVenta(rs.getDouble("precio_venta"));
                lote.setPasillo(rs.getString("pasillo"));
                lote.setEstante(rs.getString("estante"));
                lote.setIdEstado(rs.getInt("id_estado"));
                lote.setNombreProducto(rs.getString("nombre_producto"));
                lote.setNombreProveedor(rs.getString("nombre_proveedor"));
                lote.setNombreEstado(rs.getString("nombre_estado"));
                lotes.add(lote);
            }
        }
        return lotes;
    }

    // Esto pertenece al punto 2 del taller de practica
    public List<Lot> obtenerComprasDelDia(LocalDate fecha) throws SQLException {
        List<Lot> compras = new ArrayList<>();
        String sql = "SELECT l.id_lote, l.cantidad_inicial, l.precio_compra, p.nombre AS nombre_proveedor "
                + "FROM lotes_inventario l "
                + "JOIN proveedores p ON l.id_proveedor = p.id_proveedor "
                + "WHERE DATE(l.fecha_ingreso) = ? "
                + "ORDER BY l.id_lote";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, java.sql.Date.valueOf(fecha));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Lot lote = new Lot();
                    lote.setIdLote(rs.getInt("id_lote"));
                    lote.setCantidadInicial(rs.getDouble("cantidad_inicial"));
                    lote.setPrecioCompra(rs.getDouble("precio_compra"));
                    lote.setNombreProveedor(rs.getString("nombre_proveedor"));
                    compras.add(lote);
                }
            }
        }
        return compras;
    }

    // ------------------------------------------------------------------

    public List<Lot> obtenerLotesConProveedor() throws SQLException{
        List<Lot> lotes = new ArrayList<>();
        String sql = "SELECT \n" + //
                        "    l.id_lote,\n" + //
                        "    l.cantidad_inicial,\n" + //
                        "    e.nombre AS estado_lote,\n" + //
                        "    p.id_proveedor,\n" + //
                        "    p.nombre AS nombre_proveedor\n" + //
                        "FROM lotes_inventario l\n" + //
                        "INNER JOIN proveedores p ON l.id_proveedor = p.id_proveedor\n" + //
                        "INNER JOIN estados_lote e ON l.id_estado = e.id_estado";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()){
            while (rs.next()) {
                Lot lote = new Lot();
                lote.setIdLote(rs.getInt("id_lote"));
                lote.setCantidadInicial(rs.getDouble("cantidad_inicial"));
                lote.setNombreEstado(rs.getString("estado_lote"));
                lote.setIdProveedor(rs.getInt("id_proveedor"));
                lote.setNombreProveedor(rs.getString("nombre_proveedor"));
                lotes.add(lote);
            }
        }
        return lotes;
    }

}
