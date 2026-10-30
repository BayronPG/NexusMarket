# Sustentación de NexusMarket

> Documento preparado el **2026-10-21** para exponer el estado real del proyecto.
> Actualizado el **2026-10-29** con la Fase 2.4 (manejo global de errores) y los metadatos OpenAPI (2.6).
> Fuente de verdad: código de `main` (`e7d2c1e`, igual a `origin/main`) y documentos de `docs/`.
> La secuencia REST fue verificada contra los controladores y DTO reales; **no se ejecutó contra una
> instancia MySQL en esta sesión**.

## 1. Evidencias académicas

| Evidencia | Peso | Qué se muestra | Artefactos principales | Verificación | Estado |
|---|---|---|---|---|---|
| **E1 - Modelo de dominio del negocio** | 10 % | Participantes, conceptos, relaciones, reglas y validaciones | `docs/modelo-de-dominio.md`, `src/main/java/com/nexusmarket/domain/` | Modelo documentado + clases de dominio | Entregado |
| **Parcial individual de modelado** | 20 % | Decisiones de modelado y trazabilidad requisito → componente | `docs/modelo-de-dominio.md`, `docs/trazabilidad.md`, `domain/` | Matriz de trazabilidad OBJ/RN/RG | Realizado |
| **E2 - Servicios de dominio y/o aplicación** | 10 % | 6 servicios de aplicación, puertos de entrada/salida y pruebas | `docs/servicios-de-dominio.md`, `application/`, `src/test/java/.../application/` | 6 clases de servicio + pruebas con JUnit 5/AssertJ | Implementado |
| **Incremento de persistencia + API Web (Evidencia 5)** | 20 % | JPA/Hibernate, Flyway, MySQL, 6 controladores y 15 endpoints | `infrastructure/persistence/jpa/`, `infrastructure/web/`, `db/migration/`, `docs/PLAN_API_WEB.md` | `mvn -B test`: 165 pruebas, 0 fallos | **En curso**: faltan 2.7 y 2.8 (2.6 resuelta con metadatos) |
| **Seguridad real (Unidad 4)** | — | Spring Security y autenticación real | — | — | Fuera de este alcance |

El incremento de persistencia y API se publicó en `origin/main` en `46eaef3`; la sincronización
documental quedó en `e7d2c1e`.

## 2. Guion de 8 a 10 minutos

| Tiempo | Qué decir / mostrar | Archivo o comando |
|---|---|---|
| 0:00-1:00 | Encuadre: tres entregables y un incremento que los conecta. Aclarar que la Evidencia 5 está en curso. | `README.md` |
| 1:00-3:00 | Modelo: participantes, conceptos, reglas. Señalar la convención **EXPLICIT / INTERPRETATION / PENDING** y la trazabilidad. | `docs/modelo-de-dominio.md` §3, §4, §6-§9, §12 |
| 3:00-4:00 | Reglas dentro del dominio, no en los servicios: ciclo del pedido y stock. | `domain/order/Order.java`, `domain/inventory/Inventory.java` |
| 4:00-5:00 | Arquitectura: `domain` → `application` (puertos) → `infrastructure` (JPA y web). Mostrar que el dominio no depende de Spring. | `application/port/`, `infrastructure/persistence/jpa/adapter/`, `infrastructure/web/controller/` |
| 5:00-8:00 | Demo REST: vendedor → bodega → producto → stock → reserva → comprador (SQL) → pedido → reembolso. | §5 de este documento |
| 8:00-9:00 | Pruebas y trazabilidad: resultado real de Maven y matriz requisito → prueba. | §6, `docs/trazabilidad.md` |
| 9:00-10:00 | Limitaciones declaradas y cierre: qué falta para cerrar la Evidencia 5. | §7 |

## 3. Arquitectura `domain → application → infrastructure`

```text
domain/                     Reglas e invariantes. Sin Spring, sin JPA, sin HTTP.
   ▲
   │  (los adaptadores dependen de los puertos; el dominio no conoce a nadie)
application/                Casos de uso, ActorContext, puertos de entrada y salida.
   ▲
   │
infrastructure/             Adaptadores: web (REST) y persistencia (JPA | memory).
```

- `domain/`: entidades y reglas (por ejemplo `Order.changeStatus` para RN-13/RN-16 e
  `Inventory.reserve` para RN-10/RN-11/RN-12).
- `application/port/in`: contratos que expone la aplicación (`ManageCatalogUseCase`, etc.).
- `application/port/out`: contratos que la aplicación necesita (`ProductRepository`, etc.).
- `application/service`: implementación de los casos de uso; autorización y validación de entrada.
- `infrastructure/persistence/jpa`: entidades JPA, mappers, repositorios Spring Data y adaptadores
  que implementan los puertos de salida.
- `infrastructure/persistence/memory`: adaptadores en memoria para el perfil `memory`.
- `infrastructure/web`: controladores, DTO, mappers web y resolución temporal de `ActorContext`.

**Regla de dependencia:** `infrastructure` → `application` → `domain`. El dominio no importa Spring,
JPA ni clases web. El esquema lo gobierna Flyway (`spring.jpa.hibernate.ddl-auto=validate`).

## 4. Prerrequisitos y comandos de PowerShell

### 4.1 Pruebas

```powershell
mvn -B test
```

### 4.2 Base de datos y arranque

Requisitos: MySQL 8 en ejecución y la base `nexusmarket` creada. Flyway crea las tablas; Hibernate
solo valida (`ddl-auto=validate`).

```powershell
mysql --user=root --password -e "CREATE DATABASE IF NOT EXISTS nexusmarket;"

$env:DB_URL = "jdbc:mysql://127.0.0.1:3306/nexusmarket"
$env:DB_USER = "root"
$env:DB_PASSWORD = "<tu-contraseña>"

mvn -B spring-boot:run
```

La aplicación queda en `http://localhost:8080`. Para la demo se necesita además el cliente `mysql`
porque **no existe endpoint para registrar compradores** (§5.6).

## 5. Secuencia de demo REST verificada

### 5.1 Cabeceras obligatorias

Todas las peticiones a `/api/**` requieren estas dos cabeceras; el `ActorContext` se construye a
partir de ellas:

| Cabecera | Valor | Ejemplo |
|---|---|---|
| `X-Actor-Id` | identificador del usuario que ejecuta la operación | `1`, `2` |
| `X-Actor-Role` | rol único del usuario (enum `Role`) | `ADMINISTRATOR`, `SELLER`, `BUYER`, `LOGISTICS_OPERATOR`, `SUPERVISOR` |

Esto **no es autenticación**: el llamador declara su identidad y su rol. Si falta una cabecera o el rol
no existe, la respuesta es **400** con el cuerpo uniforme de error de la Fase 2.4.

### 5.2 Endpoints reales (15)

| Método | Ruta | Rol requerido | Cuerpo | Respuesta |
|---|---|---|---|---|
| POST | `/api/sellers` | `ADMINISTRATOR` | `fullName`, `email`, `identityDocument` | `id`, `userId` |
| GET | `/api/sellers/{sellerId}` | `ADMINISTRATOR` | — | `id`, `userId` |
| POST | `/api/warehouses` | `ADMINISTRATOR` | `type`; `sellerId` condicional | `id`, `type`, `sellerId` |
| GET | `/api/warehouses?sellerId={id}` | `ADMINISTRATOR` | — | lista de bodegas |
| POST | `/api/products` | `SELLER` | `type`, `status`, `variants` (opcional) | `id`, `sellerId`, `type`, `variants`, `status` |
| PATCH | `/api/products/{productId}/status` | `SELLER` | `status` | producto actualizado |
| GET | `/api/products/published` | cualquier actor autenticado | — | lista de productos publicados |
| GET | `/api/products/mine` | `SELLER` | — | lista de productos del vendedor |
| POST | `/api/inventories` | `SELLER`, `LOGISTICS_OPERATOR` | `productId`, `warehouseId`, `quantity` | `id`, `productId`, `warehouseId`, `quantity`, `damagedQuantity` |
| POST | `/api/inventories/reservations` | `SELLER`, `LOGISTICS_OPERATOR` | `productId`, `warehouseId`, `amount` | inventario actualizado |
| GET | `/api/inventories?productId={id}` | `SELLER`, `LOGISTICS_OPERATOR` | — | lista de inventarios |
| POST | `/api/orders` | `BUYER` | — | `id`, `buyerId`, `status` |
| PATCH | `/api/orders/{orderId}/status` | `BUYER`, `SELLER`, `LOGISTICS_OPERATOR` | `status` | pedido actualizado |
| GET | `/api/orders/{orderId}` | `BUYER`, `SELLER`, `LOGISTICS_OPERATOR` | — | `id`, `buyerId`, `status` |
| POST | `/api/refunds` | `BUYER`, `ADMINISTRATOR` | `orderId` | `id`, `orderId` |

Valores válidos de los enums usados en la demo:

- `WarehouseType`: `MARKETPLACE`, `SELLER`.
- `ProductType`: `PHYSICAL`, `DIGITAL`.
- `ProductStatus`: `PUBLISHED`, `SUSPENDED`, `DISCONTINUED`.
- `OrderStatus`: `CART`, `PENDING_PAYMENT`, `PAID`, `SHIPPED`, `DELIVERED`.

La transición del pedido es secuencial: `CART` → `PENDING_PAYMENT` → `PAID` → `SHIPPED` → `DELIVERED`.
No se puede saltar un estado ni modificar un pedido `DELIVERED`.

### 5.3 Script completo

Los identificadores se **capturan de las respuestas**; no se escriben a mano. Ejecutar en una base
vacía o con un sufijo nuevo (el sufijo evita choques con correos y documentos ya registrados).

```powershell
$base = "http://localhost:8080"
$admin = @{ "X-Actor-Id" = "1"; "X-Actor-Role" = "ADMINISTRATOR" }
$suffix = Get-Date -Format "yyyyMMddHHmmss"

# 1. Registrar vendedor (ADMINISTRATOR). Devuelve id y userId.
$seller = Invoke-RestMethod -Method Post -Uri "$base/api/sellers" -Headers $admin -ContentType "application/json" -Body (@{
    fullName = "Vendedor Demo"
    email = "seller+$suffix@nexus.test"
    identityDocument = "CC-$suffix"
} | ConvertTo-Json)

$sellerHeaders = @{ "X-Actor-Id" = "$($seller.userId)"; "X-Actor-Role" = "SELLER" }

# 2. Bodega del vendedor y bodega del Marketplace.
$warehouse = Invoke-RestMethod -Method Post -Uri "$base/api/warehouses" -Headers $admin -ContentType "application/json" -Body (@{
    type = "SELLER"
    sellerId = $seller.id
} | ConvertTo-Json)

$marketplace = Invoke-RestMethod -Method Post -Uri "$base/api/warehouses" -Headers $admin -ContentType "application/json" -Body '{"type":"MARKETPLACE"}'

# 3. Registrar producto publicado.
$product = Invoke-RestMethod -Method Post -Uri "$base/api/products" -Headers $sellerHeaders -ContentType "application/json" -Body '{"type":"PHYSICAL","status":"PUBLISHED","variants":["Color: negro"]}'

# 4. Registrar existencias y reservar 3 unidades.
$stock = Invoke-RestMethod -Method Post -Uri "$base/api/inventories" -Headers $sellerHeaders -ContentType "application/json" -Body (@{
    productId = $product.id
    warehouseId = $warehouse.id
    quantity = 10
} | ConvertTo-Json)

$reserved = Invoke-RestMethod -Method Post -Uri "$base/api/inventories/reservations" -Headers $sellerHeaders -ContentType "application/json" -Body (@{
    productId = $product.id
    warehouseId = $warehouse.id
    amount = 3
} | ConvertTo-Json)

Invoke-RestMethod -Method Get -Uri "$base/api/inventories?productId=$($product.id)" -Headers $sellerHeaders
Invoke-RestMethod -Method Get -Uri "$base/api/products/mine" -Headers $sellerHeaders
Invoke-RestMethod -Method Get -Uri "$base/api/products/published" -Headers $sellerHeaders

# 5. Cambiar estado del producto.
Invoke-RestMethod -Method Patch -Uri "$base/api/products/$($product.id)/status" -Headers $sellerHeaders -ContentType "application/json" -Body '{"status":"SUSPENDED"}'

# 6. Comprador: no hay endpoint de registro; se inserta por SQL y se captura su userId.
$mysqlBase = @("--host=127.0.0.1", "--port=3306", "--user=$env:DB_USER", "--password=$env:DB_PASSWORD")
$buyerEmail = "buyer+$suffix@nexus.test"
$sqlBuyer = "INSERT INTO users (full_name,email,identity_document,role,status) VALUES ('Comprador Demo','$buyerEmail','CCB-$suffix','BUYER','ACTIVE'); INSERT INTO buyers (user_id,primary_address,commercial_status) VALUES (LAST_INSERT_ID(),'Calle 1 #2-3','ACTIVE');"
& mysql @mysqlBase -e $sqlBuyer nexusmarket

$buyerUserId = (& mysql @mysqlBase -N -B -e "SELECT id FROM users WHERE email='$buyerEmail';" nexusmarket).Trim()
$buyerHeaders = @{ "X-Actor-Id" = "$buyerUserId"; "X-Actor-Role" = "BUYER" }

# 7. Crear pedido y avanzar el ciclo hasta PAID.
$order = Invoke-RestMethod -Method Post -Uri "$base/api/orders" -Headers $buyerHeaders
$order = Invoke-RestMethod -Method Patch -Uri "$base/api/orders/$($order.id)/status" -Headers $buyerHeaders -ContentType "application/json" -Body '{"status":"PENDING_PAYMENT"}'
$order = Invoke-RestMethod -Method Patch -Uri "$base/api/orders/$($order.id)/status" -Headers $buyerHeaders -ContentType "application/json" -Body '{"status":"PAID"}'

# 8. Reembolso del pedido y consultas finales.
$refund = Invoke-RestMethod -Method Post -Uri "$base/api/refunds" -Headers $buyerHeaders -ContentType "application/json" -Body (@{ orderId = $order.id } | ConvertTo-Json)

Invoke-RestMethod -Method Get -Uri "$base/api/orders/$($order.id)" -Headers $buyerHeaders
Invoke-RestMethod -Method Get -Uri "$base/api/sellers/$($seller.id)" -Headers $admin
Invoke-RestMethod -Method Get -Uri "$base/api/warehouses?sellerId=$($seller.id)" -Headers $admin
```

### 5.4 Qué se espera en cada paso

| Paso | Respuesta esperada |
|---|---|
| 1 | `id` y `userId` del vendedor; se usan en `$seller.id` y `$seller.userId`. |
| 2 | Bodega `SELLER` con `sellerId`; bodega `MARKETPLACE` con `sellerId` nulo. |
| 3 | Producto con `status = PUBLISHED` y `sellerId` del vendedor. |
| 4 | Inventario con `quantity = 10` y, tras reservar, `quantity = 7`. |
| 5 | Producto con `status = SUSPENDED`; deja de aparecer en `/published`. |
| 6 | Usuario `BUYER` y fila en `buyers`; `$buyerUserId` es el `X-Actor-Id` del comprador. |
| 7 | Pedido en `CART`, luego `PENDING_PAYMENT` y `PAID`. |
| 8 | Reembolso con `orderId` del pedido y `id` generado. |

### 5.5 Verificación de la secuencia contra el código

| Paso | Controlador | DTO real | Campos comprobados |
|---|---|---|---|
| 1 | `SellerController.register` | `CreateSellerRequest` | `fullName`, `email`, `identityDocument` (`@NotBlank`) |
| 2 | `WarehouseController.register` | `CreateWarehouseRequest` | `type`; `sellerId` obligatorio para `SELLER` y nulo para `MARKETPLACE` |
| 3 | `CatalogController.register` | `CreateProductRequest` | `type`, `status` (`@NotNull`), `variants` opcional |
| 4 | `InventoryController.register` / `reserve` | `RegisterStockRequest` / `ReserveStockRequest` | `productId`, `warehouseId`, `quantity` / `amount` |
| 5 | `CatalogController.changeStatus` | `ChangeProductStatusRequest` | `status` (`@NotNull`) |
| 6 | — | — | No hay endpoint; inserción SQL directa en `users` y `buyers` |
| 7 | `OrderController.create` / `changeStatus` | `ChangeOrderStatusRequest` | Sin cuerpo al crear; `status` (`@NotNull`) al cambiar |
| 8 | `RefundController.request` | `CreateRefundRequest` | `orderId` (`@NotNull`) |

## 6. Resultado real de las pruebas

Comando ejecutado:

```powershell
mvn -B test
```

Salida real (2026-10-27, tras la Fase 2.4):

```text
[INFO] Tests run: 164, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

- La suite por defecto ejecuta **164 pruebas** (155 previas + 9 de `GlobalExceptionHandlerTest`).
- `PersistenceMysqlIntegrationTest` contiene **10 pruebas** con `@Tag("mysql")` y queda excluida por
  defecto (`pom.xml`), porque exige una instancia MySQL real.
- Para ejecutarla:

```powershell
mvn -B test -Dtest=PersistenceMysqlIntegrationTest -DexcludedTestTags=none
```

## 7. Limitaciones conocidas

1. **No hay autenticación real.** `ActorContext` se construye desde `X-Actor-Id` y `X-Actor-Role`; el
   llamador puede declarar cualquier identidad o rol. La seguridad corresponde a la Unidad 4.
2. **El pedido no tiene líneas, cantidades ni total.** `Order` solo guarda `buyerId` y `status`; el
   `Cart` existe en el dominio pero no está conectado al flujo REST.
3. **No hay endpoint para registrar compradores ni administradores.** La demo crea el comprador por
   SQL. El administrador se simula con una cabecera, sin fila en `users`.
4. **Asociaciones como columnas `Long` (P-1).** No hay claves foráneas ni asociaciones JPA; el
   esquema definitivo lo gobierna Flyway. `users.email` e `users.identity_document` no tienen
   restricción `UNIQUE` en la base: la unicidad RN-14/RN-15 se valida solo en el servicio.
5. **MySQL local es 5.7.24, no 8.0.** El arranque y las pruebas HTTP contra MySQL real funcionaron,
   pero Hibernate advierte que 5.7 ya no es soportado (mínimo 8.0). La suite `mysql` sigue excluida
   del build por defecto.
6. **Sin OpenAPI, colección Postman, pruebas E2E, paginación, versionado de API, CI ni
   Testcontainers.** Son tareas pendientes/fuera de alcance de esta preparación.
7. **Conteo histórico en `docs/servicios-de-dominio.md` §9.** Allí figura el corte de la Evidencia 2
   (67 pruebas al 2026-10-13). El conteo vigente es el de la §6 de este documento.

## 8. Preguntas probables y respuestas breves

| Pregunta | Respuesta breve |
|---|---|
| ¿Por qué no hay Spring Security? | La seguridad es la Unidad 4. En esta evidencia `RG-01` se asume con un `ActorContext` temporal y documentado (D-019); no se afirma que sea autenticación real. |
| ¿Por qué `X-Actor-Id` y `X-Actor-Role`? | Permiten demostrar autorización por rol sin ampliar el alcance. Son provisionales y se reemplazan por el usuario autenticado en la evidencia de seguridad. |
| ¿Por qué el dominio no tiene anotaciones JPA? | Para mantener el dominio independiente del framework. Las entidades JPA y los mappers viven en `infrastructure/persistence/jpa/`. |
| ¿Por qué `Long` en lugar de `@ManyToOne`? | Decisión P-1: agregados separados y esquema versionado con Flyway. Las asociaciones definitivas son deuda declarada. |
| ¿Dónde se cumplen las reglas del negocio? | En el dominio cuando dependen de una entidad (`Order.changeStatus`, `Inventory.reserve`) y en los servicios cuando dependen de estado externo (unicidad RN-14/15, existencia de referencias). |
| ¿Por qué H2 si la base real es MySQL? | H2 se usa **solo** en el perfil `test`. La configuración real es MySQL por variables de entorno; la suite MySQL se ejecuta aparte con `-DexcludedTestTags=none`. |
| ¿El sistema ya gestiona pedidos completos? | No. `Order` no tiene ítems ni total; está declarado como pendiente. La demo muestra el ciclo de estados, no una compra con productos. |
| ¿Por qué un POST devuelve 200 y no 201? | Es un ajuste de semántica HTTP pendiente; no forma parte de las tareas de esta preparación. |
| ¿Qué falta para cerrar la Evidencia 5? | OpenAPI (2.6), colección Postman (2.7) y pruebas E2E (2.8), además de la validación contra MySQL real. |
| ¿La bodega Marketplace puede crearse sin vendedor? | Sí. `CreateWarehouseRequest` valida condicionalmente: `SELLER` exige `sellerId`; `MARKETPLACE` exige que sea nulo. Hay pruebas para ambos casos. |

## 9. Alcance de la verificación de este documento

- Rutas, métodos, roles, DTO y campos: verificados contra
  `infrastructure/web/controller/*.java`, `infrastructure/web/dto/*.java` y
  `application/service/*.java`.
- Reglas de transición y stock: verificadas contra `domain/order/Order.java` y
  `domain/inventory/Inventory.java`.
- Resultado de pruebas: tomado de la ejecución real de `mvn -B test` (2026-10-21).
- No se ejecutó la aplicación ni la secuencia REST contra MySQL en esta sesión; por eso la demo debe
  ensayarse una vez antes de la sustentación.
