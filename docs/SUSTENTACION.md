# Sustentación de NexusMarket

> Documento actualizado el **2026-11-10** con el incremento de envío, devolución y reembolso por
> ítem (V5). Fuente de verdad: código de `main` y documentos de `docs/`.
> La secuencia REST fue verificada contra los controladores y DTO reales y contra H2 con
> `SustentationFlowE2ETest`; **no se ejecutó contra MySQL en esta sesión**. Ensaye el guion una vez
> antes de la sustentación.

## 1. Evidencias académicas

| Evidencia | Peso | Qué se muestra | Artefactos principales | Verificación | Estado |
|---|---|---|---|---|---|
| **E1 - Modelo de dominio del negocio** | 10 % | Participantes, conceptos, relaciones, reglas y validaciones | `docs/modelo-de-dominio.md`, `src/main/java/com/nexusmarket/domain/` | Modelo documentado + clases de dominio | Entregado |
| **Parcial individual de modelado** | 20 % | Decisiones de modelado y trazabilidad requisito → componente | `docs/modelo-de-dominio.md`, `docs/trazabilidad.md`, `domain/` | Matriz de trazabilidad OBJ/RN/RG | Realizado |
| **E2 - Servicios de dominio y/o aplicación** | 10 % | Servicios de aplicación, puertos de entrada/salida y pruebas | `docs/servicios-de-dominio.md`, `application/`, `src/test/java/.../application/` | Servicios + pruebas con JUnit 5/AssertJ | Implementado |
| **Incremento de persistencia + API Web (Evidencia 5)** | 20 % | JPA/Hibernate, Flyway, MySQL, 8 controladores y 27 endpoints | `infrastructure/persistence/jpa/`, `infrastructure/web/`, `db/migration/`, `docs/PLAN_API_WEB.md` | `mvn -B verify`: **278 pruebas, 0 fallos** | **Completas**: fases 2.1-2.12 |
| **Seguridad real (Unidad 4)** | — | Spring Security y autenticación real | `infrastructure/security/`, `AuthController` | Login JWT + resource server | Implementado |

## 2. Guion de 8 a 10 minutos

| Tiempo | Qué decir / mostrar | Archivo o comando |
|---|---|---|
| 0:00-1:00 | Encuadre: tres entregables y un incremento que los conecta. Estado real de la Evidencia 5. | `README.md` |
| 1:00-3:00 | Modelo: participantes, conceptos, reglas. Convención **EXPLICIT / INTERPRETATION / PENDING**. | `docs/modelo-de-dominio.md` §3, §4, §6-§9, §12 |
| 3:00-4:00 | Reglas dentro del dominio: ciclo del pedido, stock y estado por línea. | `domain/order/Order.java`, `domain/order/OrderItem.java`, `domain/inventory/Inventory.java` |
| 4:00-5:00 | Arquitectura: `domain` → `application` (puertos) → `infrastructure` (JPA y web). | `application/port/`, `infrastructure/persistence/jpa/adapter/`, `infrastructure/web/controller/` |
| 5:00-8:00 | Demo REST del flujo exigido: 3 vendedores, 1 pedido, pago, envío por vendedor, entrega, devolución y reembolso parcial. | §5.3 de este documento |
| 8:00-9:00 | Pruebas y trazabilidad: resultado real de Maven y flujo E2E de sustentación. | §6, `SustentationFlowE2ETest` |
| 9:00-10:00 | Limitaciones declaradas y cierre. | §7 |

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

- `domain/`: entidades y reglas (por ejemplo `Order.changeStatus`, `OrderItem.ship` e
  `Inventory.reserve`).
- `application/port/in`: contratos que expone la aplicación (`ManageOrdersUseCase`, etc.).
- `application/port/out`: contratos que la aplicación necesita (`ReturnRepository`, etc.).
- `application/service`: implementación de los casos de uso; autorización y validación de entrada.
- `infrastructure/persistence/jpa`: entidades JPA, mappers, repositorios Spring Data y adaptadores.
- `infrastructure/persistence/memory`: adaptadores en memoria para el perfil `memory`.
- `infrastructure/web`: controladores, DTO, mappers web y resolución del `ActorContext`.

**Regla de dependencia:** `infrastructure` → `application` → `domain`. El dominio no importa Spring,
JPA ni clases web. El esquema lo gobierna Flyway (`spring.jpa.hibernate.ddl-auto=validate`).

## 4. Prerrequisitos y comandos de PowerShell

### 4.1 Pruebas

```powershell
mvn -B verify
```

### 4.2 Base de datos y arranque

Requisitos: MySQL 8 en ejecución y la base `nexusmarket` creada. Flyway crea las tablas; Hibernate
solo valida (`ddl-auto=validate`).

```powershell
mysql --user=root --password -e "CREATE DATABASE IF NOT EXISTS nexusmarket;"

$env:DB_URL = "jdbc:mysql://127.0.0.1:3306/nexusmarket"
$env:DB_USER = "root"
$env:DB_PASSWORD = "<tu-contraseña>"
$env:JWT_SECRET = "<secreto-de-al-menos-32-bytes>"

mvn -B spring-boot:run
```

La aplicación queda en `http://localhost:8080`. Para la demo se necesita el cliente `mysql` porque
**no existe endpoint para registrar compradores** (§5.6).

## 5. Secuencia de demo REST

### 5.1 Autenticación obligatoria (JWT)

Todas las peticiones a `/api/**` requieren un token JWT válido en `Authorization: Bearer <jwt>`. El
`ActorContext` se construye desde los claims `sub` (id `Long` positivo), `role`, `iat` y `exp`.
Sin token, firma inválida, expiración o claims inválidos → **401**; rol no permitido → **403**.
`POST /api/auth/login` emite el token si el usuario tiene `password_hash`; para usuarios de
demostración sin contraseña, el guion firma el token localmente con `JWT_SECRET`.

### 5.2 Endpoints reales (27)

| Grupo | Endpoints |
|---|---|
| Auth (1) | `POST /api/auth/login` |
| Seller (2) | `POST /api/sellers`, `GET /api/sellers/{id}` |
| Warehouse (2) | `POST /api/warehouses`, `GET /api/warehouses?sellerId=` |
| Catalog (4) | `POST /api/products`, `PATCH /api/products/{id}/status`, `GET /api/products/published`, `GET /api/products/mine` |
| Inventory (3) | `POST /api/inventories`, `POST /api/inventories/reservations`, `GET /api/inventories?productId=` |
| Order (8) | `POST /api/orders`, `POST /api/orders/{id}/items`, `PATCH`/`DELETE /api/orders/{id}/items/{itemId}`, `PATCH /api/orders/{id}/status`, `PATCH .../items/{itemId}/ship`, `PATCH .../items/{itemId}/deliver`, `GET /api/orders/{id}` |
| Return (5) | `POST /api/returns`, `GET /api/returns/{id}`, `GET /api/returns?orderId=`, `PATCH /api/returns/{id}/approve`, `PATCH /api/returns/{id}/receive` |
| Refund (2) | `GET /api/refunds/{id}`, `GET /api/refunds?orderId=` |

Roles de la demo:

- `POST /api/sellers` y `POST /api/warehouses`: `ADMINISTRATOR`.
- Productos, stock y `.../ship`: `SELLER` dueño del producto.
- Pedido, pago, devolución: `BUYER` propietario.
- `.../deliver`, `.../receive`: `LOGISTICS_OPERATOR`.
- `.../approve`: `ADMINISTRATOR`.
- `GET /api/orders/{id}`: comprador propietario, vendedor o logística; los dos últimos ven el pedido
  completo.

### 5.3 Guion exacto de sustentación (un pedido, tres vendedores)

Cada acción va seguida de su GET de comprobación. Los identificadores se capturan de las respuestas;
no se escriben a mano.

```powershell
$base = "http://localhost:8080"
$suffix = Get-Date -Format "yyyyMMddHHmmss"
$secret = $env:JWT_SECRET
if (-not $secret -or $secret.Length -lt 32) { throw "Defina JWT_SECRET (>=32 bytes)" }

function New-Token([string]$sub, [string]$role) {
    function B64Url([string]$text) {
        [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes($text)).TrimEnd('=').Replace('+','-').Replace('/','_')
    }
    $now = [DateTimeOffset]::UtcNow.ToUnixTimeSeconds()
    $header = B64Url '{"alg":"HS256","typ":"JWT"}'
    $payload = B64Url (@{ sub = $sub; role = $role; iat = $now; exp = $now + 3600 } | ConvertTo-Json -Compress)
    $data = "$header.$payload"
    $hmac = [System.Security.Cryptography.HMACSHA256]::new([Text.Encoding]::UTF8.GetBytes($secret))
    $sig = [Convert]::ToBase64String($hmac.ComputeHash([Text.Encoding]::UTF8.GetBytes($data))).TrimEnd('=').Replace('+','-').Replace('/','_')
    "$data.$sig"
}

$admin = @{ Authorization = "Bearer $(New-Token '1' 'ADMINISTRATOR')" }

# ---------------------------------------------------------------------------
# PASO 1. Tres vendedores, cada uno con su bodega.
# ---------------------------------------------------------------------------
$sellers = @()
1..3 | ForEach-Object {
    $seller = Invoke-RestMethod -Method Post -Uri "$base/api/sellers" -Headers $admin -ContentType "application/json" -Body (@{
        fullName = "Vendedor $_ $suffix"; email = "seller$_+$suffix@nexus.test"; identityDocument = "CC-$_-$suffix"
    } | ConvertTo-Json)
    $warehouse = Invoke-RestMethod -Method Post -Uri "$base/api/warehouses" -Headers $admin -ContentType "application/json" -Body (@{
        type = "SELLER"; sellerId = $seller.id
    } | ConvertTo-Json)
    $sellers += [pscustomobject]@{
        id = $seller.id; userId = $seller.userId; warehouseId = $warehouse.id
        token = (New-Token "$($seller.userId)" 'SELLER')
    }
}
# GET de comprobación: vendedor y bodega
Invoke-RestMethod -Method Get -Uri "$base/api/sellers/$($sellers[0].id)" -Headers $admin
Invoke-RestMethod -Method Get -Uri "$base/api/warehouses?sellerId=$($sellers[0].id)" -Headers $admin

# ---------------------------------------------------------------------------
# PASO 2. Tres productos (uno por vendedor), precio distinto y stock 10.
# ---------------------------------------------------------------------------
$prices = @(10.00, 20.00, 30.00)
$products = @()
for ($i = 0; $i -lt 3; $i++) {
    $sellerHeaders = @{ Authorization = "Bearer $($sellers[$i].token)" }
    $product = Invoke-RestMethod -Method Post -Uri "$base/api/products" -Headers $sellerHeaders -ContentType "application/json" -Body (@{
        type = "PHYSICAL"; status = "PUBLISHED"; price = $prices[$i]
    } | ConvertTo-Json)
    $stock = Invoke-RestMethod -Method Post -Uri "$base/api/inventories" -Headers $sellerHeaders -ContentType "application/json" -Body (@{
        productId = $product.id; warehouseId = $sellers[$i].warehouseId; quantity = 10
    } | ConvertTo-Json)
    $products += [pscustomobject]@{ id = $product.id; price = $prices[$i]; token = $sellers[$i].token }
}
# GET de comprobación: stock inicial 10 por producto
$products | ForEach-Object {
    (Invoke-RestMethod -Method Get -Uri "$base/api/inventories?productId=$($_.id)" -Headers @{ Authorization = "Bearer $($_.token)" }).content[0] |
        Select-Object productId, quantity, damagedQuantity
}

# ---------------------------------------------------------------------------
# PASO 3. Comprador por SQL (no hay endpoint de registro) y su JWT.
# ---------------------------------------------------------------------------
$mysqlBase = @("--host=127.0.0.1", "--port=3306", "--user=$env:DB_USER", "--password=$env:DB_PASSWORD")
$buyerEmail = "buyer+$suffix@nexus.test"
& mysql @mysqlBase nexusmarket -e "INSERT INTO users (full_name,email,identity_document,role,status) VALUES ('Comprador Demo','$buyerEmail','CCB-$suffix','BUYER','ACTIVE'); INSERT INTO buyers (user_id,primary_address,commercial_status) VALUES (LAST_INSERT_ID(),'Calle 1 #2-3','ACTIVE');"
$buyerUserId = (& mysql @mysqlBase -N -B nexusmarket -e "SELECT id FROM users WHERE email='$buyerEmail';").Trim()
$buyer = @{ Authorization = "Bearer $(New-Token "$buyerUserId" 'BUYER')" }
$logistics = @{ Authorization = "Bearer $(New-Token '900' 'LOGISTICS_OPERATOR')" }

# ---------------------------------------------------------------------------
# PASO 4. Un solo pedido con tres items.
# ---------------------------------------------------------------------------
$order = Invoke-RestMethod -Method Post -Uri "$base/api/orders" -Headers $buyer
foreach ($product in $products) {
    $order = Invoke-RestMethod -Method Post -Uri "$base/api/orders/$($order.id)/items" -Headers $buyer -ContentType "application/json" -Body (@{
        productId = $product.id; quantity = 1
    } | ConvertTo-Json)
}
# GET 1: tres items, tres vendedores distintos, total 60.00
$detail = Invoke-RestMethod -Method Get -Uri "$base/api/orders/$($order.id)" -Headers $buyer
$detail | Select-Object id, status, total
$detail.items | Select-Object id, productId, sellerId, quantity, unitPrice, status

# GET 2: stock 10/10/10 antes de pagar
$products | ForEach-Object {
    (Invoke-RestMethod -Method Get -Uri "$base/api/inventories?productId=$($_.id)" -Headers @{ Authorization = "Bearer $($_.token)" }).content[0] |
        Select-Object quantity, damagedQuantity
}

# ---------------------------------------------------------------------------
# PASO 5. Pago: CART -> PENDING_PAYMENT -> PAID.
# ---------------------------------------------------------------------------
Invoke-RestMethod -Method Patch -Uri "$base/api/orders/$($order.id)/status" -Headers $buyer -ContentType "application/json" -Body '{"status":"PENDING_PAYMENT"}'
Invoke-RestMethod -Method Patch -Uri "$base/api/orders/$($order.id)/status" -Headers $buyer -ContentType "application/json" -Body '{"status":"PAID"}'

# GET 3: pedido PAID y stock 9/9/9
(Invoke-RestMethod -Method Get -Uri "$base/api/orders/$($order.id)" -Headers $buyer).status
$products | ForEach-Object {
    (Invoke-RestMethod -Method Get -Uri "$base/api/inventories?productId=$($_.id)" -Headers @{ Authorization = "Bearer $($_.token)" }).content[0] |
        Select-Object quantity
}

# ---------------------------------------------------------------------------
# PASO 6. Cada vendedor despacha SOLO su item.
# ---------------------------------------------------------------------------
for ($i = 0; $i -lt 3; $i++) {
    Invoke-RestMethod -Method Patch -Uri "$base/api/orders/$($order.id)/items/$($detail.items[$i].id)/ship" -Headers @{ Authorization = "Bearer $($sellers[$i].token)" }
}
# GET 4: pedido SHIPPED y los tres items SHIPPED
$detail = Invoke-RestMethod -Method Get -Uri "$base/api/orders/$($order.id)" -Headers $buyer
$detail.status
$detail.items | Select-Object productId, sellerId, status

# Prueba negativa: el vendedor 1 intenta despachar el item del vendedor 2 -> 403
try {
    Invoke-RestMethod -Method Patch -Uri "$base/api/orders/$($order.id)/items/$($detail.items[2].id)/ship" -Headers @{ Authorization = "Bearer $($sellers[0].token)" }
} catch { "$([int]$_.Exception.Response.StatusCode) (esperado 403)" }

# ---------------------------------------------------------------------------
# PASO 7. Logistica confirma la entrega de los tres items.
# ---------------------------------------------------------------------------
foreach ($item in $detail.items) {
    Invoke-RestMethod -Method Patch -Uri "$base/api/orders/$($order.id)/items/$($item.id)/deliver" -Headers $logistics
}
# GET 5: pedido DELIVERED y los tres items DELIVERED
$detail = Invoke-RestMethod -Method Get -Uri "$base/api/orders/$($order.id)" -Headers $buyer
$detail.status
$detail.items | Select-Object productId, status

# ---------------------------------------------------------------------------
# PASO 8. Devolucion del SEGUNDO producto (cantidad 1, RESELLABLE).
# ---------------------------------------------------------------------------
$return = Invoke-RestMethod -Method Post -Uri "$base/api/returns" -Headers $buyer -ContentType "application/json" -Body (@{
    orderId = $order.id; orderItemId = $detail.items[1].id; quantity = 1
    reason = "Producto defectuoso"; condition = "RESELLABLE"
} | ConvertTo-Json)
# GET 6: devolucion REQUESTED
Invoke-RestMethod -Method Get -Uri "$base/api/returns/$($return.id)" -Headers $buyer

# Aprobacion (ADMINISTRATOR) y recepcion (LOGISTICS_OPERATOR)
Invoke-RestMethod -Method Patch -Uri "$base/api/returns/$($return.id)/approve" -Headers $admin
# GET 7: devolucion APPROVED
Invoke-RestMethod -Method Get -Uri "$base/api/returns/$($return.id)" -Headers $buyer

$receipt = Invoke-RestMethod -Method Patch -Uri "$base/api/returns/$($return.id)/receive" -Headers $logistics
# GET 8: devolucion RECEIVED con refundId
Invoke-RestMethod -Method Get -Uri "$base/api/returns/$($return.id)" -Headers $buyer

# ---------------------------------------------------------------------------
# PASO 9. Reembolso parcial y comprobacion final.
# ---------------------------------------------------------------------------
# GET 9: reembolso con el precio historico del segundo producto (20.00)
Invoke-RestMethod -Method Get -Uri "$base/api/refunds/$($receipt.refundId)" -Headers $buyer
# GET 10: solo un reembolso para el pedido
Invoke-RestMethod -Method Get -Uri "$base/api/refunds?orderId=$($order.id)" -Headers $buyer

# GET 11: inventario final A=9, B=10, C=9
$products | ForEach-Object {
    (Invoke-RestMethod -Method Get -Uri "$base/api/inventories?productId=$($_.id)" -Headers @{ Authorization = "Bearer $($_.token)" }).content[0] |
        Select-Object productId, quantity, damagedQuantity
}
```

### 5.4 Qué se espera en cada paso

| Paso | Respuesta esperada |
|---|---|
| 1 | Vendedor con `id` y `userId`; bodega `SELLER` con `sellerId`. |
| 2 | Producto `PUBLISHED` con `price` y `sellerId`; inventario con `quantity = 10`. |
| 3 | Usuario `BUYER` y fila en `buyers`; `$buyerUserId` es el `sub` del token. |
| 4 | Pedido `CART` con 3 items, 3 `sellerId` distintos, `total = 60.00`. |
| 5 | `PAID`; el stock baja a 9 en los tres productos. |
| 6 | `SHIPPED`; los 3 items `SHIPPED`; un vendedor ajeno recibe 403. |
| 7 | `DELIVERED`; los 3 items `DELIVERED`. |
| 8 | Devolución `REQUESTED` → `APPROVED` → `RECEIVED`, con `refundId`. |
| 9 | Reembolso de 20.00 (precio histórico del segundo item); inventario 9/10/9. |

### 5.5 Verificación de la secuencia contra el código

| Paso | Controlador / servicio | DTO real |
|---|---|---|
| 1 | `SellerController.register`, `WarehouseController.register` | `CreateSellerRequest`, `CreateWarehouseRequest` |
| 2 | `CatalogController.register`, `InventoryController.register` | `CreateProductRequest`, `RegisterStockRequest` |
| 4 | `OrderController.create` / `addItem` | `AddOrderItemRequest` |
| 5 | `OrderController.changeStatus` (`OrderService.changeOrderStatus`) | `ChangeOrderStatusRequest` |
| 6 | `OrderController.shipItem` (`OrderService.shipItem`) | sin cuerpo; `itemId` en la ruta |
| 7 | `OrderController.deliverItem` | sin cuerpo; `itemId` en la ruta |
| 8 | `ReturnController.request` / `approve` / `receive` | `CreateReturnRequest` |
| 9 | `RefundController.find` / `findByOrder` | `RefundResponse` |

### 5.6 Limitaciones conocidas

1. **Sin pasarela de pago**: `PAID` cambia el estado y descuenta inventario; no hay confirmación
   financiera externa. `RefundStatus.REGISTERED` registra la obligación, no una transferencia.
2. **Sin registro público de compradores ni administradores**: la demo crea el comprador por SQL y
   firma su token con `JWT_SECRET`.
3. **Sin recurso `Shipment`**: el envío es un estado por línea (`SHIPPED`/`DELIVERED`); no hay
   transportadora, guía ni seguimiento.
4. **`REJECTED` sin endpoint**: el estado existe en el dominio, pero la API mínima solo expone
   aprobar y recibir.
5. **Asociaciones como columnas `Long` (P-1)**: sin claves foráneas JPA; el esquema lo gobierna
   Flyway. Las claves foráneas existen en las tablas nuevas (V5).
6. **MySQL local es 5.7.24, no 8.0**: el arranque y las pruebas HTTP funcionaron; Hibernate advierte
   que 5.7 está fuera de soporte. La suite `mysql` sigue excluida del build por defecto.
7. **Sin versionado de API, CI ni Testcontainers**: deuda declarada.
8. **`GET /api/orders/{id}`**: el comprador solo puede leer sus propios pedidos (403 en otro caso);
   vendedor y logística leen cualquier pedido, porque no existe un listado de pedidos por vendedor.

### 5.7 Preguntas probables y respuestas breves

| Pregunta | Respuesta breve |
|---|---|
| ¿Cómo se autentica la API? | `POST /api/auth/login` valida BCrypt y emite un JWT HS256; el resource server lo valida y el `ActorContext` sale de `sub` y `role`. Sin token → 401; rol no permitido → 403. |
| ¿Un pedido puede tener productos de varios vendedores? | Sí. Cada `OrderItem` referencia un `Product`; el `sellerId` se deriva al leer y se expone en el GET. |
| ¿Quién despacha? | Solo el `SELLER` dueño del producto de esa línea; el estado global no permite que un vendedor despache todo el pedido. |
| ¿Quién confirma la entrega? | `LOGISTICS_OPERATOR`, línea por línea. |
| ¿Cómo se devuelve solo un producto? | La devolución referencia `orderItemId`; el reembolso se calcula con la cantidad devuelta y el `unitPrice` histórico de esa línea. |
| ¿El reembolso mueve dinero? | No. No hay pasarela; se registra la obligación con estado `REGISTERED`. |
| ¿Qué pasa con el inventario al devolver? | `RESELLABLE` repone `quantity`; `DAMAGED` aumenta `damagedQuantity`; se registra un `InventoryMovement` tipo `RETURN`. Repetir la recepción no repone dos veces. |
| ¿Por qué el pago descuenta stock y no lo reserva? | Decisión del incremento: no hay reserva temporal; el descuento y el estado se confirman en una transacción con bloqueo pesimista. |

## 6. Resultado real de las pruebas

```text
[INFO] Tests run: 278, Failures: 0, Errors: 0, Skipped: 0
[INFO] All coverage checks have been met.
[INFO] BUILD SUCCESS
```

- Cobertura: **87.7 % líneas / 67.1 % ramas** (umbral 80/65).
- `SustentationFlowE2ETest`: 3 pruebas (flujo completo, reglas negativas, dañado e idempotencia).
- `PersistenceMysqlIntegrationTest`: 10 pruebas con `@Tag("mysql")`, excluidas por defecto.

```powershell
mvn -B test -Dtest=PersistenceMysqlIntegrationTest -DexcludedTestTags=none
```

## 7. Alcance de la verificación de este documento

- Rutas, métodos, roles, DTO y campos: verificados contra `infrastructure/web/controller/*.java`,
  `infrastructure/web/dto/*.java` y `application/service/*.java`.
- Reglas de transición, stock y devolución: verificadas contra `domain/order/Order.java`,
  `domain/order/OrderItem.java`, `domain/inventory/Inventory.java` y
  `application/service/ReturnService.java`.
- Flujo completo: ejecutado en H2 con `SustentationFlowE2ETest` (278/278 en verde).
- **No se ejecutó contra MySQL en esta sesión**; ensaye el guion §5.3 una vez antes de la
  sustentación y confirme el motor en el log de arranque.
