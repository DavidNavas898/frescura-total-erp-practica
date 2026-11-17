package com.frescuratotal.erp.sales.model;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class CustomerCache {

    private static CustomerCache instancia;

    private Map<Integer, Customer> clientes;
    private CustomerDao customerDao;

    private CustomerCache() {
        clientes = new HashMap<>();
        customerDao = new CustomerDao();
    }

    public static CustomerCache getInstance() {
        if (instancia == null) {
            instancia = new CustomerCache();
        }
        return instancia;
    }

    public boolean estaEnCache(int idCliente) {
        return clientes.containsKey(idCliente);
    }

    public Customer obtenerCliente(int idCliente) throws SQLException {
        if (clientes.containsKey(idCliente)) {
            return clientes.get(idCliente);
        }

        Customer cliente = customerDao.obtenerClientePorId(idCliente);
        if (cliente != null) {
            clientes.put(idCliente, cliente);
        }
        return cliente;
    }
}
