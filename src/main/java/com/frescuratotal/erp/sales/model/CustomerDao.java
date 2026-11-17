package com.frescuratotal.erp.sales.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.frescuratotal.erp.common.DatabaseConnection;

public class CustomerDao {
    public List<Customer> obtenerClientes() throws SQLException {
        List<Customer> clientes = new ArrayList<>();
        String sql = "SELECT c.*, t.nombre AS nombre_tipo "
                + "FROM clientes c "
                + "JOIN tipos_cliente t ON c.id_tipo = t.id_tipo "
                + "ORDER BY c.nombre";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Customer cliente = new Customer();
                cliente.setIdCliente(rs.getInt("id_cliente"));
                cliente.setNombre(rs.getString("nombre"));
                cliente.setDocumento(rs.getString("documento"));
                cliente.setEmail(rs.getString("email"));
                cliente.setTelefono(rs.getString("telefono"));
                cliente.setDireccion(rs.getString("direccion"));
                cliente.setIdTipo(rs.getInt("id_tipo"));
                cliente.setFechaRegistro(rs.getTimestamp("fecha_registro"));
                cliente.setNombreTipo(rs.getString("nombre_tipo"));
                clientes.add(cliente);
            }
        }
        return clientes;
    }

    public void registrarCliente(Customer cliente) throws SQLException {
        String sql = "INSERT INTO clientes (nombre, documento, email, telefono, direccion, id_tipo) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cliente.getNombre());
            stmt.setString(2, cliente.getDocumento());
            stmt.setString(3, cliente.getEmail());
            stmt.setString(4, cliente.getTelefono());
            stmt.setString(5, cliente.getDireccion());
            stmt.setInt(6, cliente.getIdTipo());
            stmt.executeUpdate();
        }
    }

    public Customer obtenerClientePorId(int idCliente) throws SQLException {
        String sql = "SELECT c.*, t.nombre AS nombre_tipo "
                + "FROM clientes c "
                + "JOIN tipos_cliente t ON c.id_tipo = t.id_tipo "
                + "WHERE c.id_cliente = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idCliente);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Customer cliente = new Customer();
                    cliente.setIdCliente(rs.getInt("id_cliente"));
                    cliente.setNombre(rs.getString("nombre"));
                    cliente.setDocumento(rs.getString("documento"));
                    cliente.setEmail(rs.getString("email"));
                    cliente.setTelefono(rs.getString("telefono"));
                    cliente.setDireccion(rs.getString("direccion"));
                    cliente.setIdTipo(rs.getInt("id_tipo"));
                    cliente.setFechaRegistro(rs.getTimestamp("fecha_registro"));
                    cliente.setNombreTipo(rs.getString("nombre_tipo"));
                    return cliente;
                }
            }
        }
        return null;
    }

    public boolean existeDocumento(String documento) throws SQLException {
        String sql = "SELECT id_cliente FROM clientes WHERE documento = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, documento);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public int registrarClientes(List<Customer> clientes) throws SQLException {
        String sql = "INSERT INTO clientes (nombre, documento, email, telefono, id_tipo) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                for (Customer cliente : clientes) {
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setString(1, cliente.getNombre());
                        stmt.setString(2, cliente.getDocumento());
                        stmt.setString(3, cliente.getEmail());
                        stmt.setString(4, cliente.getTelefono());
                        stmt.setInt(5, cliente.getIdTipo());
                        stmt.executeUpdate();
                    }
                }
                conn.commit();
                return clientes.size();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public List<Customer> obtenerClientesPorLote(int idLote) throws SQLException {
        List<Customer> clientes = new ArrayList<>();
        String sql = "SELECT DISTINCT c.id_cliente, c.nombre, c.documento, c.telefono "
                + "FROM clientes c "
                + "JOIN ventas v ON v.id_cliente = c.id_cliente "
                + "JOIN detalles_venta d ON d.id_venta = v.id_venta "
                + "WHERE d.id_lote = ? "
                + "ORDER BY c.nombre";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idLote);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Customer cliente = new Customer();
                    cliente.setIdCliente(rs.getInt("id_cliente"));
                    cliente.setNombre(rs.getString("nombre"));
                    cliente.setDocumento(rs.getString("documento"));
                    cliente.setTelefono(rs.getString("telefono"));
                    clientes.add(cliente);
                }
            }
        }
        return clientes;
    }
}
