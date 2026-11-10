# NexusMarket

Proyecto académico de **Construcción de Software 2** — Tecnológico de Antioquia (2026-2).

- **Estudiante:** Jhon Bayron Peláez Guerra
- **Docente:** Andrés Felipe Sánchez Aguiar
- **Programa:** Ingeniería de Software
- **Grupo:** Martes y jueves 6:00 pm

## Descripción

NexusMarket es una plataforma digital centralizada que actúa como intermediario comercial entre compradores y vendedores. Administra la operación desde el registro de usuarios y la publicación de productos hasta la logística, la facturación y la posventa.

## Entregables

| Entregable | Contenido | Fecha |
|---|---|---|
| **E1 — Modelo de dominio del negocio** | [docs/modelo-de-dominio.md](docs/modelo-de-dominio.md) + clases de dominio en `src/main/java/com/nexusmarket/domain/` | 2026-08-30 |
| **E2 — Servicios de dominio y/o aplicación** | [docs/servicios-de-dominio.md](docs/servicios-de-dominio.md) + servicios de aplicación en `src/main/java/com/nexusmarket/application/` + pruebas automatizadas | 2026-09-27 |

### Incremento de API Web (Evidencia 5) — en curso

Incremento posterior a E2, **publicado** en `origin/main` (`46eaef3`, 2026-10-15):

- **Persistencia completa (Fases 1.1–1.7)**: 8 entidades JPA, 8 mappers, 8 repositorios Spring Data y
  **8 adaptadores JPA** de los puertos de salida; los adaptadores en memoria se conservan para pruebas
  bajo el perfil `memory`.
- **Esquema con Flyway** (`V1`–`V3`) y `spring.jpa.hibernate.ddl-auto=validate`.
- **Borde REST (Fases 2.1–2.4 y 2.5)**: 6 controladores sobre los 6 puertos de entrada
  (**18 endpoints**), DTO y mappers web, validación `@Valid` (400), manejo global de errores con
  respuesta uniforme y `ActorContext` resuelto desde el JWT autenticado.
- **Pedidos con ítems (2026-11-10)**: carrito con líneas (`OrderItem`), precio histórico,
  subtotales y total; inventario descontado de forma atómica al pagar (detalle en "Pedidos con ítems").
- **Seguridad (Unidad 4)**: resource server JWT `stateless` (HS256, secreto externo, autoridad
  `ROLE_<role>`); 401 sin token o con token inválido, 403 cuando la regla de rol del caso de uso lo impide.
- **OpenAPI (2.6)**: los 15 endpoints documentados con `@Tag`, `@Operation` y `@ApiResponse`, con el
  esquema `bearerAuth` como requisito global; `/v3/api-docs` y Swagger UI verificados con `OpenApiDocsTest`.
- **Postman (2.7)**: `docs/postman/NexusMarket.postman_collection.json` (6 carpetas, 18 requests).
- **Pruebas end-to-end (2.8)**: tres flujos reales (catálogo, pedidos y devoluciones) en `ApiFlowE2ETest`.
- **Pendiente**: emisión de tokens y login (fuera de esta evidencia; ver "Seguridad").
- **Asociaciones JPA**: siguen como columnas escalares `Long` (P-1); el esquema definitivo es Flyway.
- Plan detallado: [docs/PLAN_API_WEB.md](docs/PLAN_API_WEB.md).

## Stack técnico

- **Java 17** (LTS) y **Maven**.
- **Spring Boot 3.4.5** (Web, Data JPA).
- **Arquitectura de puertos y adaptadores**: `domain` · `application` · `infrastructure`.
- **MySQL** como base de datos **real** (configurada por variables de entorno).
- **Flyway** como fuente única del esquema (migraciones en `db/migration`).
- **H2** exclusivamente para **pruebas** (perfil `test`, en modo compatibilidad MySQL).
- **Spring Boot Validation** (`@Valid`) para la validación de entrada del borde REST.
- **Spring Security** + **OAuth2 Resource Server** (validación JWT HS256).
- **JUnit 5** y **AssertJ**.

## Estructura del repositorio

```
NexusMarket/
├── README.md
├── pom.xml
├── docs/
│   ├── modelo-de-dominio.md       # Entregable 1
│   ├── servicios-de-dominio.md    # Entregable 2
│   ├── trazabilidad.md            # Matriz de trazabilidad
│   └── PLAN_API_WEB.md            # Plan de la API Web (Evidencia 5)
└── src/
    ├── main/java/com/nexusmarket/
    │   ├── domain/                # Entregable 1: modelo de dominio (sin dependencias de framework)
    │   │   ├── user/ buyer/ seller/ warehouse/ catalog/
    │   │   ├── inventory/ order/ billing/ logistics/ returns/ reporting/
    │   ├── application/           # Entregable 2: casos de uso y servicios
    │   │   ├── port/in/           # Puertos de entrada (casos de uso)
    │   │   ├── port/out/          # Puertos de salida (repositorios)
    │   │   ├── service/           # Servicios que implementan los casos de uso
    │   │   └── error/             # Excepciones de la capa de aplicación
    │   └── infrastructure/        # Adaptadores
    │       ├── persistence/
    │       │   ├── memory/        # Adaptadores en memoria (perfil "memory", solo pruebas)
    │       │   └── jpa/
    │       │       ├── entity/    # 8 entidades JPA (separadas del dominio)
    │       │       ├── mapper/    # 8 mappers dominio <-> JPA
    │       │       ├── repository/# 8 repositorios Spring Data
    │       │       └── adapter/   # 8 adaptadores JPA de los puertos de salida
    │       └── web/               # Adaptadores de entrada (borde REST)
    │           ├── controller/    # 6 controladores (15 endpoints)
    │           ├── dto/           # DTO de entrada y salida
    │           └── mapper/        # Mapeo dominio <-> DTO
    ├── main/resources/application.properties        # MySQL (variables de entorno)
    ├── main/resources/db/migration/                 # Migraciones Flyway (V1__initial_schema.sql)
    ├── test/java/com/nexusmarket/                   # Pruebas
    ├── test/resources/application-test.properties   # Perfil de pruebas (H2, MODE=MySQL)
    └── test/resources/application-mysql.properties  # Integración contra MySQL real
```

## Cómo ejecutar

Requisitos: **JDK 17** y **Maven 3.9+**.

```powershell
mvn test               # compila el proyecto y ejecuta las pruebas (usa H2, perfil "test")
mvn spring-boot:run    # inicia la aplicación Spring Boot contra MySQL
```

La ejecución real requiere configurar **MySQL** por **variables de entorno** (sin credenciales en el repositorio):

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/nexusmarket"; $env:DB_USER="..."; $env:DB_PASSWORD="..."
mvn spring-boot:run
```

Las **pruebas** no necesitan MySQL: usan **H2 en memoria** mediante el perfil `test`
(`src/test/resources/application-test.properties`, en modo compatibilidad MySQL).

La prueba de integración de persistencia contra **MySQL real** está excluida del build por defecto
(etiqueta `mysql`) y requiere una instancia accesible y credenciales:

```powershell
$env:DB_URL="jdbc:mysql://127.0.0.1:3307/nexusmarket"; $env:DB_USER="..."; $env:DB_PASSWORD="..."
mvn -B test -Dtest=PersistenceMysqlIntegrationTest -DexcludedTestTags=none
```

## Arquitectura

El proyecto sigue una organización por capas con puertos y adaptadores:

- **Dominio (`domain`)**: entidades y reglas de negocio, sin anotaciones de framework.
- **Aplicación (`application`)**: casos de uso (puertos de entrada), servicios que los implementan y puertos de salida para la persistencia.
- **Infraestructura (`infrastructure`)**: adaptadores de salida (`persistence/jpa` es la
  implementación por defecto; `persistence/memory` solo bajo el perfil `memory`) y adaptadores de
  entrada (`web` con controladores, DTO y mappers).

Las reglas de negocio que una entidad puede verificar por sí misma permanecen en el dominio
(`Inventory.reserve`, `Order.changeStatus`); los servicios de aplicación las orquestan y no las
duplican.

## Estado y pendientes (2026-11-10)

- **Base de datos real: MySQL** (D-004 resuelta por el estudiante). **MongoDB no se implementa**.
- **Persistencia cerrada (Fases 1.1–1.7)**: los adaptadores **JPA son la implementación por defecto**
  de los 8 puertos de salida; los `InMemory*` quedan **solo para pruebas** bajo el perfil `memory`.
- **Esquema**: lo crea **Flyway** (`V1`–`V3`; V3 agrega `products.price` y `order_items`); Hibernate solo valida
  (`spring.jpa.hibernate.ddl-auto=validate`).
- **Borde REST (Fases 2.1–2.4 y 2.5)**: 6 controladores, 18 endpoints, DTO y mappers web, validación
  de entrada (`@Valid` → 400), manejo global de errores (`@RestControllerAdvice` → 400/401/403/404/409/500)
  y `ActorContext` resuelto desde los claims del JWT (Unidad 4).
- **Evidencia 5**: 2.6 (OpenAPI de los 18 endpoints), 2.7 (colección Postman), 2.8 (pruebas E2E de
  los tres flujos reales) y 2.9 (paginación de los cuatro listados) resueltas.
- **Pedidos con ítems (2.10)**: `POST /api/orders/{id}/items`, `PATCH`/`DELETE` de un ítem y detalle
  con `items` y `total`. Reglas: solo el comprador propietario, solo productos publicados con precio,
  cantidad positiva y acotada por el inventario, precio histórico inmutable y pago sin doble descuento.
- **Paginación (2.9)**: los listados aceptan `page` (≥0), `size` (1-100, por defecto 20) y
  `sort` (`campo` o `campo,asc|desc`); responden `content`, `page`, `size`, `totalElements` y
  `totalPages`. Filtros opcionales: `sellerId` y `status` en productos, `warehouseId` en inventario.
- **Calidad**: JaCoCo mide en la fase `test` y `mvn verify` exige ≥ 80 % líneas y ≥ 65 % ramas
  (actual: 88.1 % líneas y 72.5 % ramas).
- **Decisiones provisionales** (no definitivas): relaciones como columnas `Long` (P-1), conservar
  `@ElementCollection` (P-2), validaciones de negocio en la capa de aplicación (P-3) y precio del
  producto como atributo nuevo (**D-PRICE**, no descrito por la especificación; necesario para el
  precio histórico del pedido).
- **Pruebas:** `mvn -B clean verify` → **252/252 verdes** (incluye `jacoco:check`); integración contra MySQL 8.0 → **10/10**.
- **Decisiones, supuestos y alcance real** del incremento: [docs/servicios-de-dominio.md](docs/servicios-de-dominio.md).
- Preguntas abiertas del dominio (variantes, contenido del pedido, catálogo de estados,
  reportes): [docs/modelo-de-dominio.md](docs/modelo-de-dominio.md), sección 11.

## Pedidos con ítems (2026-11-10)

- **Modelo**: `OrderItem` (`id`, `orderId`, `productId`, `quantity`, `unitPrice`). `unitPrice` es una
  instantánea del precio vigente del producto al agregarlo; los cambios posteriores de catálogo no la
  alteran. El total es la suma de `quantity * unitPrice`.
- **Migración Flyway**: `V3__order_items_and_product_price.sql`. Agrega `products.price`
  (`DECIMAL(12,2)`, por defecto `0.00` para filas existentes) y la tabla `order_items` con
  `UNIQUE(order_id, product_id)`, claves foráneas a `orders` y `products` e índices por `order_id` y
  `product_id`. **V2 ya existía** (índices de la fase 2.9), por eso este cambio es V3.
- **Precio (D-PRICE)**: la especificación no define el precio del producto; se agrega como atributo
  mínimo necesario para el pedido. Escala 2 y redondeo `HALF_UP`. Si se envía `price` al registrar,
  debe ser mayor que `0` (400); los productos heredados con precio `0` no se pueden agregar al carrito.
- **Endpoints**: `POST /api/orders/{orderId}/items` (`productId`, `quantity`),
  `PATCH /api/orders/{orderId}/items/{itemId}` (`quantity`), `DELETE /api/orders/{orderId}/items/{itemId}`
  y `GET /api/orders/{orderId}` con `items` (id, productId, quantity, unitPrice, subtotal) y `total`.
- **Reglas**: solo el `BUYER` propietario edita el carrito (403 en otro caso); solo productos
  publicados y con precio; cantidad positiva y nunca superior al inventario disponible; repetir un
  producto incrementa la línea; un carrito vacío no pasa a `PENDING_PAYMENT` ni `PAID`; los ítems solo
  se editan en `CART`; `PAID` no retrocede.
- **Inventario y concurrencia**: el cambio a `PAID` y el descuento de stock ocurren en una sola
  transacción (`@Transactional`). El pedido se bloquea con `PESSIMISTIC_WRITE` y las filas de
  inventario también (`lockByProductId`), de modo que dos pagos concurrentes no venden de más ni
  descuentan dos veces; repetir `PAID` es idempotente. El descuento recorre las bodegas en orden de id.
- **Limitaciones**: no hay pasarela de pagos ni confirmación financiera externa, no hay reserva
  temporal de stock (se descuenta al pagar) y no hay edición de precio por variante.

## Seguridad (Unidad 4)

- **Resource Server JWT `stateless`**: sin login, sin HTTP Basic y sin sesión. Toda petición a
  `/api/**` exige `Authorization: Bearer <jwt>`.
- **Algoritmo y secreto**: HS256. El secreto se lee de la variable de entorno `JWT_SECRET` (mínimo
  32 bytes UTF-8); la aplicación falla al arrancar si falta o es más corto. **No se versiona ningún
  secreto real**: el valor de `application-test.properties` es un valor falso exclusivo de pruebas.
- **Claims**: `sub` = id `Long` positivo del usuario (en formato String), `role` = valor exacto del
  enum `Role`, más `iat`/`exp` estándar.
- **Autoridad**: el claim `role` se convierte en `ROLE_<role>` (por ejemplo `ROLE_SELLER`).
- **ActorContext**: se construye desde el JWT; las cabeceras `X-Actor-Id` y `X-Actor-Role` fueron
  eliminadas.
- **Rutas públicas**: `/v3/api-docs/**`, `/swagger-ui/**` y `/swagger-ui.html`. El resto requiere token.
- **Respuestas**: 401 si falta el token, es inválido, está expirado o sus claims no son válidos;
  403 cuando el rol no está permitido para el caso de uso (RG-03).
- **Prueba manual**: generar un token HS256 en <https://jwt.io> con el mismo `JWT_SECRET` y los
  claims indicados (por ejemplo `{"sub":"25","role":"BUYER","iat":...,"exp":...}`) y enviarlo como
  `Authorization: Bearer <token>`.
- **Limitaciones**: esta evidencia no incluye emisión de tokens, login, refresh ni rotación de
  secreto. El secreto es simétrico y compartido, y la autorización por rol se aplica en los servicios
  de aplicación, no con reglas de URL.

## Fuentes oficiales

- Especificación Funcional del Negocio: NexusMarket.
- Formato para la Concertación de la Evaluación de los Aprendizajes (FO-DOC-108).
- Microcurrículo Construcción de Software II 2026-2.
