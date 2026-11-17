package com.frescuratotal.erp;

import com.frescuratotal.erp.common.view.MainView;
import com.frescuratotal.erp.inventory.controller.ProductController;
import com.frescuratotal.erp.inventory.model.ProductDao;
import com.frescuratotal.erp.inventory.view.ProductView;
import com.frescuratotal.erp.suppliers.controller.SupplierController;
import com.frescuratotal.erp.suppliers.model.SupplierDao;
import com.frescuratotal.erp.suppliers.view.SupplierView;
import com.frescuratotal.erp.inventory.controller.LotController;
import com.frescuratotal.erp.inventory.model.LotDao;
import com.frescuratotal.erp.inventory.model.QuarantineDao;
import com.frescuratotal.erp.inventory.model.InspectionDao;
import com.frescuratotal.erp.inventory.model.WasteDao;
import com.frescuratotal.erp.inventory.view.LotView;
import com.frescuratotal.erp.sales.controller.CustomerController;
import com.frescuratotal.erp.sales.model.CustomerDao;
import com.frescuratotal.erp.sales.view.CustomerView;
import com.frescuratotal.erp.sales.controller.SaleController;
import com.frescuratotal.erp.sales.model.SaleDao;
import com.frescuratotal.erp.sales.model.InvoiceDao;
import com.frescuratotal.erp.sales.view.SaleView;
import com.frescuratotal.erp.logistics.controller.LogisticsController;
import com.frescuratotal.erp.logistics.model.DriverDao;
import com.frescuratotal.erp.logistics.model.RouteDao;
import com.frescuratotal.erp.logistics.model.VehicleDao;
import com.frescuratotal.erp.logistics.view.LogisticsView;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        MainView mainView = new MainView(scanner);
        SupplierController supplierController =
                new SupplierController(new SupplierDao(), new ProductDao(), new SupplierView(scanner), new LotDao());
        ProductController productController =
                new ProductController(new ProductDao(), new ProductView(scanner));
        LotController lotController =
                new LotController(new LotDao(), new LotView(scanner), new ProductDao(), new SupplierDao(),
                        new QuarantineDao(), new InspectionDao(), new WasteDao(), new CustomerDao());
        CustomerController customerController =
                new CustomerController(new CustomerDao(), new CustomerView(scanner));
        SaleController saleController =
                new SaleController(new SaleDao(), new SaleView(scanner), new CustomerDao(), new ProductDao(),
                        new LotDao(), new InvoiceDao());
        LogisticsController logisticsController =
                new LogisticsController(new DriverDao(), new VehicleDao(), new RouteDao(),
                        new LogisticsView(scanner));
        int opcion;
        do {
            opcion = mainView.mostrarMenu();
            switch (opcion) {
                case 1:
                    supplierController.iniciar();
                    break;
                case 2:
                    productController.iniciar();
                   break;
                case 3:
                    lotController.iniciar();
                    break;
                case 4:
                    customerController.iniciar();
                    break;
                case 5:
                    saleController.iniciar();
                    break;
                case 6:
                    logisticsController.iniciar();
                    break;
                case 7:
                    lotController.generarReporte();
                    break;
                case 8:
                    lotController.generarReporteCompras();
                    break;
                case 9:
                    saleController.generarEstadisticas();
                    break;
                case 0:
                    mainView.mostrarMensaje("Hasta pronto.");
                    break;
                default:
                    mainView.mostrarMensaje("Opcion invalida.");
            }
        } while (opcion != 0);

        scanner.close();
    }
}
