# ERP para la Bodega de Alimentos "Frescura Total"

## Descripción
Aplicación de consola en Java que centraliza la operación de la bodega de alimentos
"Frescura Total". Resuelve las pérdidas por vencimiento, la falta de trazabilidad, el
desorden de inventario y la facturación manual, conectando cuatro módulos: **inventario y
calidad** (recepción de mercancía por lotes, alertas de vencimiento, cuarentena,
inspección y mermas), **proveedores y compras** (directorio, catálogo e historial de
entregas), **ventas y facturación** (clientes mayoristas y minoristas, venta con
estrategia FEFO, promoción automática del 50 % para lotes próximos a vencer, descuento
automático de stock y factura electrónica en PDF con CUFE y código QR simulados) y
**logística y entregas** (conductores, vehículos, panel de despachos, rutas de entrega y
seguimiento del pedido hasta su entrega).

## Autores
- Juan David Navas Jurado

## Requerimientos Previos
- **Java JDK 17**
- **Apache Maven 3.8+** (incluido en Apache NetBeans)
- **MySQL Server 8.0**
- **Apache NetBeans IDE 12 o superior**
- Un cliente de MySQL para ejecutar el script (MySQL Workbench, DBeaver o la consola `mysql`)
- **Git**

Las librerías externas se descargan automáticamente con Maven:

| Librería | Uso |
|---|---|
| `mysql-connector-j` 8.4.0 | Conexión JDBC con MySQL |
| `openpdf` 2.0.3 | Generación de la factura en PDF |
| `zxing core / javase` 3.5.3 | Generación del código QR de la factura |

## Configuración de la Base de Datos
1. Iniciar el servidor de MySQL 8 en local.
2. Abrir el script **`docs/database.sql`** en MySQL Workbench o DBeaver y **ejecutarlo
   completo**. Desde la consola también se puede hacer así:
   ```bash
   mysql -u root -p < docs/database.sql
   ```
   El script:
   - borra y crea la base de datos `erp_frescura_total` (sección **DDL**),
   - crea las 22 tablas con sus relaciones,
   - carga los catálogos y los datos de prueba coherentes (sección **DML**: 50 proveedores,
     60 productos, 55 clientes, 80 lotes, 60 ventas con sus detalles y facturas, rutas, etc.).

   > ⚠️ El script comienza con `DROP DATABASE IF EXISTS`, así que borra cualquier versión
   > anterior de `erp_frescura_total`.

3. Configurar las credenciales en la clase
   `src/main/java/com/frescuratotal/erp/common/DatabaseConnection.java`:
   ```java
   private static final String URL = "jdbc:mysql://localhost:3306/erp_frescura_total";
   private static final String USER = "root";
   private static final String PASSWORD = "tu_contraseña";
   ```

## Instalación y Ejecución
### Clonar el repositorio
```bash
git clone https://github.com/DavidNavas898/frescura-total-erp.git
```

### Abrir en Apache NetBeans
1. **File > Open Project** y seleccionar la carpeta `frescura-total-erp`. NetBeans la
   reconoce como proyecto Maven por el archivo `pom.xml`.
2. Esperar a que Maven descargue las dependencias (se ve en la barra inferior).

### Compilar
- Menú **Run > Clean and Build Project** (o **Shift + F11**).

### Ejecutar
- Menú **Run > Run Project** (o **F6**). La clase principal es `com.frescuratotal.erp.Main`.
- La aplicación muestra el menú principal en la consola de salida:
  ```
  === FRESCURA TOTAL ERP ===
  1. Proveedores
  2. Productos
  3. Inventario (lotes)
  4. Clientes
  5. Ventas
  6. Logistica y entregas
  0. Salir
  ```

### Ejecutar desde la terminal (opcional)
```bash
mvn clean compile exec:java
```

### Facturas generadas
Al confirmar una venta, la factura se guarda en PDF en la carpeta `facturas/`, en la raíz
del proyecto (por ejemplo, `facturas/FE-20261103-205605.pdf`). La carpeta se crea
automáticamente la primera vez que se genera una factura.

### Registro de eventos (logs)
El sistema guarda un registro de las acciones importantes y de los errores en el archivo
`erp.log`, en la raíz del proyecto. El archivo se crea solo la primera vez y cada nueva
línea se añade al final. Cada línea tiene la fecha y hora, el tipo (`INFO` o `ERROR`) y
el mensaje:

```
2026-11-04T11:44:03.101615200 | INFO | Lote registrado: L-20261104-114403
2026-11-04T11:48:18.480912200 | ERROR | Error al registrar cliente: Duplicate entry '900111222-5' for key 'clientes.documento'
2026-11-04T11:50:50.154086700 | INFO | Venta registrada: V-20261104-115040, total 385560.0
```

Se registran: proveedores agregados, lotes registrados, mermas, clientes registrados,
ventas, errores al generar el PDF y rutas de entrega creadas, además de los errores de base
de datos de cada una de esas operaciones.

## Estructura del Proyecto
El código sigue una arquitectura **MVC por componentes**: cada módulo del negocio tiene
sus propios paquetes `model`, `view` y `controller`.

```
src/main/java/com/frescuratotal/erp/
├── common/
│   ├── DatabaseConnection.java   // Conexión JDBC a MySQL
│   ├── LogManager.java           // Escribe los eventos y errores en erp.log
│   └── view/MainView.java        // Menú principal
├── inventory/                    // Módulo 1: Inventario y calidad
│   ├── model/      // Product, Category, Lot, Quarantine, Inspection, Waste y sus DAO
│   ├── view/       // ProductView, LotView
│   └── controller/ // ProductController, LotController
├── suppliers/                    // Módulo 2: Proveedores y compras
│   ├── model/      // Supplier, SupplierDao
│   ├── view/       // SupplierView
│   └── controller/ // SupplierController
├── sales/                        // Módulo 3: Ventas y facturación
│   ├── model/      // Customer, Sale, SaleDetail, Invoice y sus DAO
│   ├── view/       // CustomerView, SaleView, InvoicePdf
│   └── controller/ // CustomerController, SaleController
├── logistics/                    // Módulo 4: Logística y entregas
│   ├── model/      // Driver, Vehicle, DeliveryRoute, DeliveryOrder y sus DAO
│   ├── view/       // LogisticsView
│   └── controller/ // LogisticsController
└── Main.java                     // Punto de entrada: crea los controladores y muestra el menú

docs/
├── database.sql            // Script DDL + DML
├── diagrama_er.png         // Diagrama entidad-relación
└── diagrama_clases.png     // Diagrama de clases UML
```

- **Model:** las entidades del negocio (`Lot`, `Sale`...) y los DAO que las leen y guardan en MySQL.
- **View:** todo lo que se muestra o se pide al usuario por consola, más la factura en PDF (`InvoicePdf`).
- **Controller:** la lógica de cada caso de uso (FEFO, promoción, validaciones,
  cambios de estado). Recibe los datos de la vista, usa los DAO y decide qué mostrar.

Diagramas:
- [Diagrama entidad-relación](docs/diagrama_er.png)
- [Diagrama de clases UML](docs/diagrama_clases.png)

## Principios y Patrones de Diseño

### Patrón MVC (Modelo-Vista-Controlador)
Cada módulo separa los datos (`model`), la interacción con el usuario (`view`) y la lógica
(`controller`). Por ejemplo, en una venta, `SaleView` solo pide y muestra datos,
`SaleController` aplica el FEFO, la promoción y el IVA, y `SaleDao` guarda el resultado.
Esta separación permite cambiar la consola por una interfaz gráfica sin tocar la lógica ni
el acceso a datos.

### Patrón DAO (Data Access Object)
Todo el SQL está en clases `...Dao` (`LotDao`, `SaleDao`, `RouteDao`...). Los
controladores nunca escriben SQL: llaman a métodos como `obtenerLotesDisponiblesPorProducto()`
o `registrarVenta()`. Así, si cambia la base de datos o una consulta, el cambio se hace en
un solo lugar. Todos los DAO usan `PreparedStatement` con parámetros `?` para evitar la
inyección SQL.

### Principio de Responsabilidad Única (S de SOLID)
Cada clase tiene una sola razón para cambiar: las entidades solo guardan datos, los DAO
solo acceden a MySQL, las vistas solo leen y escriben en la consola, `InvoicePdf` solo
genera el PDF y `DatabaseConnection` solo crea conexiones. Por ejemplo, si cambia el diseño
de la factura, solo se modifica `InvoicePdf`.

### Inyección de dependencias por constructor (relacionado con la D de SOLID)
Los controladores no crean sus propios DAO ni vistas: los reciben en el constructor
(`new SaleController(new SaleDao(), new SaleView(scanner), ...)`) y `Main` es quien los
arma. Esto deja claras las dependencias de cada controlador y permite reutilizar el mismo
`Scanner` en toda la aplicación.

### Clase utilitaria con método de fábrica (`DatabaseConnection`)
`DatabaseConnection` tiene constructor privado y un método estático `getConnection()` que
centraliza la URL, el usuario y la contraseña. Ninguna otra clase conoce las credenciales,
y cambiarlas solo requiere editar este archivo.

### Transacciones para mantener la integridad
Las operaciones que tocan varias tablas se hacen en una sola transacción
(`setAutoCommit(false)`, `commit()` y `rollback()`):
- `SaleDao.registrarVenta()`: guarda la venta y sus detalles y descuenta el stock de cada lote.
- `RouteDao.crearRuta()`: guarda la ruta, la asignación de vehículo y conductor, y los pedidos.

Si algo falla a mitad, se deshace todo y la base de datos no queda inconsistente (por
ejemplo, una venta registrada sin haber descontado el inventario).

### Registro centralizado de eventos (`LogManager`)
`LogManager` tiene un método estático `registrar(tipo, mensaje)` que usa `FileWriter` en
modo de añadir (`append`) para escribir en `erp.log`. Los controladores solo lo llaman con
una línea, sin saber cómo ni dónde se guarda el log. Si mañana se quisiera guardar el log en
otro archivo o en la base de datos, solo se cambiaría esta clase.

### Expresiones lambda y Stream API
Algunos recorridos de listas se escribieron de forma funcional, con lambdas y streams:

| Dónde | Código | Qué hace |
|---|---|---|
| `LotView.mostrarProductos()` | `productos.forEach(producto -> ...)` | Imprime cada producto |
| `LotView.mostrarProveedores()` | `proveedores.forEach(proveedor -> ...)` | Imprime cada proveedor |
| `SupplierController` (agregar al catálogo) | `productos.stream().anyMatch(...)` | Comprueba si existe un producto con el ID escrito |
| `SaleController.cantidadYaEnPedido()` | `stream().filter(...).mapToDouble(...).sum()` | Suma lo que ya se pidió de un lote para no vender más stock del que hay |

Ejemplo:
```java
return venta.getDetalles().stream()
        .filter(detalle -> detalle.getIdLote() == idLote)
        .mapToDouble(detalle -> detalle.getCantidadVendida())
        .sum();
```

## Taller de práctica: 5 ejercicios
Este repositorio incluye los cinco ejercicios preparatorios de la guía de práctica. En el
código, cada parte está marcada con un comentario `// Esto pertenece al punto X del taller de practica`.

| Punto | Tema | Dónde se usa |
|---|---|---|
| 1 | Caché de clientes (Singleton) | Clientes → 3. Buscar cliente por ID, y al elegir el cliente en una venta nueva |
| 2 | Reporte diario de compras en CSV | Menú principal → 8 |
| 3 | Tarifas de entrega (Strategy) | Ventas → Nueva venta (pregunta la modalidad antes del resumen) |
| 4 | Importar clientes desde archivo | Clientes → 4. Importar Clientes desde Archivo |
| 5 | Estadísticas comerciales (Stream API) | Menú principal → 9 |

Adaptaciones a la base de datos del proyecto:
- **Punto 2:** no existe una tabla `compras`; cada **lote** recibido es una compra al proveedor, con
  monto = `precio_compra × cantidad_inicial`.
- **Punto 5:** los estados CONFIRMADA y DEVUELTA se representan con **Entregada** y **Cancelada**,
  que son los estados de la tabla `estados_venta`.

### Punto 1: Caché de clientes (Singleton)
`sales/model/CustomerCache.java`
```java
public class CustomerCache {

    private static CustomerCache instancia;        // 1. aquí se guarda la ÚNICA caché que existirá

    private Map<Integer, Customer> clientes;       // 2. la "libreta": ID del cliente -> datos del cliente
    private CustomerDao customerDao;

    private CustomerCache() {                       // 3. constructor PRIVADO: nadie puede hacer new CustomerCache()
        clientes = new HashMap<>();
        customerDao = new CustomerDao();
    }

    public static CustomerCache getInstance() {     // 4. la única forma de obtener la caché
        if (instancia == null) {
            instancia = new CustomerCache();        //    la primera vez se crea
        }
        return instancia;                           //    las demás veces se devuelve la misma
    }

    public Customer obtenerCliente(int idCliente) throws SQLException {
        if (clientes.containsKey(idCliente)) {      // 5. ¿ya está en la libreta?
            return clientes.get(idCliente);         //    sí -> se devuelve SIN ir a MySQL
        }
        Customer cliente = customerDao.obtenerClientePorId(idCliente);  // 6. no -> se consulta MySQL
        if (cliente != null) {
            clientes.put(idCliente, cliente);       // 7. se anota (los nulos NO se guardan)
        }
        return cliente;
    }
}
```
Así se usa, igual en `CustomerController` y en `SaleController`:
```java
CustomerCache cache = CustomerCache.getInstance();   // los dos reciben la MISMA caché
Customer cliente = cache.obtenerCliente(idCliente);
```

### Punto 2: Reporte diario de compras en CSV
`LotController.generarReporteCompras()`
```java
LocalDate hoy = LocalDate.now();                           // 1. fecha de hoy, sin preguntarle al usuario
List<Lot> compras = lotDao.obtenerComprasDelDia(hoy);      // 2. lotes que entraron hoy

PurchaseCsv purchaseCsv = new PurchaseCsv();
String archivo = purchaseCsv.generarCsv(compras, hoy);     // 3. se escribe el archivo

if (compras.isEmpty()) {                                   // 4. día sin compras: el archivo queda solo con la cabecera
    lotView.mostrarMensaje("No hay compras realizadas hoy...");
}
```
Consulta en `LotDao.obtenerComprasDelDia()`:
```sql
SELECT l.id_lote, l.cantidad_inicial, l.precio_compra, p.nombre AS nombre_proveedor
FROM lotes_inventario l
JOIN proveedores p ON l.id_proveedor = p.id_proveedor
WHERE DATE(l.fecha_ingreso) = ?        -- solo las de hoy
```
`PurchaseCsv.generarCsv()`
```java
String nombreArchivo = "compras_" + fecha + ".csv";        // compras_2026-11-20.csv

FileWriter writer = new FileWriter(nombreArchivo);
writer.write("NumeroCompra;NombreProveedor;MontoTotal\n"); // cabecera exacta
for (Lot compra : compras) {
    double monto = compra.getPrecioCompra() * compra.getCantidadInicial();
    writer.write(compra.getIdLote() + ";" + compra.getNombreProveedor() + ";" + formatearMonto(monto) + "\n");
}
writer.close();
```
Ejemplo de archivo generado (compras del 14/09/2026):
```
NumeroCompra;NombreProveedor;MontoTotal
32;Comercializadora Santander Ltda.;588000.00
40;Comercializadora Santander Ltda.;282000.00
```

### Punto 3: Tarifas de entrega (Strategy)
El contrato y las dos estrategias:
```java
public interface DeliveryStrategy {                 // 1. el contrato: toda entrega sabe calcular su total
    double calcularTotal(double valorCompra);
}

public class StandardDelivery implements DeliveryStrategy {
    private static final double RECARGO = 0.0;      // 2. estándar: 0 %
    public double calcularTotal(double valorCompra) {
        return Math.round((valorCompra + valorCompra * RECARGO) * 100) / 100.0;
    }
}

public class UrgentDelivery implements DeliveryStrategy {
    private static final double RECARGO = 0.12;     // 3. urgente: 12 %, SOLO aquí
    public double calcularTotal(double valorCompra) {
        return Math.round((valorCompra + valorCompra * RECARGO) * 100) / 100.0;
    }
}
```
`DeliveryCalculator`: el `if` solo **elige** la estrategia, no calcula.
```java
DeliveryStrategy estrategia;
if (modalidad.equals("URGENTE")) {
    estrategia = new UrgentDelivery();
} else {
    estrategia = new StandardDelivery();
}
return estrategia.calcularTotal(venta.getMontoTotal());   // 4. polimorfismo: cada una calcula a su manera
```
En `SaleController.nuevaVenta()`, antes del resumen:
```java
String modalidad = saleView.pedirModalidadEntrega();                        // pregunta 1. ESTANDAR / 2. URGENTE
double totalConEntrega = calculadora.calcularTotalConEntrega(venta, modalidad);
venta.setMontoTotal(totalConEntrega);                                      // ej: 5593.0 -> 6264.16
```
Casos de prueba: 100000 ESTANDAR → 100000 · 100000 URGENTE → 112000 · 250000 URGENTE → 280000 ·
50000 ESTANDAR → 50000 · 0 URGENTE → 0.

### Punto 4: Importar clientes desde archivo
`CustomerController.importarClientes()` (resumido; las validaciones completas están en el código):
```java
File archivo = new File("clientes_importar.txt");
if (!archivo.exists()) {                                        // 1. ¿existe el archivo?
    mostrarMensaje("Error: No se encontró el archivo clientes_importar.txt.");
    return;
}

BufferedReader lector = new BufferedReader(new FileReader(archivo));
String cabecera = lector.readLine();
if (!cabecera.equals("nombre;documento;correo;telefono;tipo_cliente")) {   // 2. cabecera exacta
    hayErrores = true;
}

while (linea != null && !hayErrores) {
    if (!linea.trim().isEmpty()) {                              // 3. las líneas vacías se saltan
        String[] campos = linea.split(";", -1);
        if (campos.length != 5) ...                              // 4. exactamente 5 campos
        else if (campo vacío) ...                                //    ninguno vacío
        else if (!esCorreoValido(correo)) ...                    //    correo con @ y punto
        else if (!tipo.equals("MAYORISTA") && !tipo.equals("MINORISTA")) ...
        else if (documentosDelArchivo.contains(documento)) ...   //    repetido en el archivo
        else if (customerDao.existeDocumento(documento)) ...     //    ya existe en MySQL
        else clientesNuevos.add(cliente);                        // 5. válido -> a la lista (todavía NO se guarda)
    }
    linea = lector.readLine();
}

if (hayErrores) {                                               // 6. un solo error cancela todo
    mostrarMensaje("Error: Se encontraron registros inválidos. Importación cancelada.");
    return;
}
int total = customerDao.registrarClientes(clientesNuevos);      // 7. se guardan todos juntos
```
`CustomerDao.registrarClientes()`: transacción de "todo o nada".
```java
conn.setAutoCommit(false);          // no guardar todavía
for (Customer cliente : clientes) {
    ... INSERT ...
}
conn.commit();                      // todo salió bien -> se guarda todo
// si algo falla:
conn.rollback();                    // se deshace todo, no quedan registros a medias
```

### Punto 5: Estadísticas comerciales (Stream API)
`SaleController.generarEstadisticas()`
```java
List<SalesRecord> registros = saleDao.obtenerRegistrosComerciales();   // 1. JOIN -> objetos SalesRecord

if (registros.isEmpty()) {                                             // 2. caso vacío
    mostrarMensaje("No existen datos suficientes para generar estadísticas comerciales.");
    return;
}

// 3. Categoría con más unidades vendidas
Map<String, Double> unidadesPorCategoria = registros.stream()
        .filter(registro -> registro.getEstadoVenta().equals("Entregada"))    // solo ventas confirmadas
        .collect(Collectors.groupingBy(
                registro -> registro.getNombreCategoria(),                    // agrupa por categoría
                Collectors.summingDouble(registro -> registro.getCantidad())  // suma las cantidades
        ));

String categoriaMayor = unidadesPorCategoria.entrySet().stream()
        .max(Map.Entry.comparingByValue())                                    // la de mayor suma
        .map(entrada -> entrada.getKey())
        .orElse("Sin ventas confirmadas");

// 4. Clientes con devoluciones, sin repetir
List<String> clientesConDevoluciones = registros.stream()
        .filter(registro -> registro.getEstadoVenta().equals("Cancelada"))    // solo devueltas
        .map(registro -> registro.getNombreCliente())                         // venta -> nombre
        .distinct()                                                           // quita repetidos
        .sorted()
        .collect(Collectors.toList());
```
Salida con los datos de prueba:
```
===== ESTADISTICAS COMERCIALES =====
Categoria con mayor volumen vendido: Bebidas - 526.0 unidades
Clientes con devoluciones:
```
