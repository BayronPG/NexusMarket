# Servicios de dominio y/o aplicación — NexusMarket

**Entregable 2 — Evidencia 2: Servicios de dominio y/o aplicación (10 %)**
Construcción de Software 2 · Tecnológico de Antioquia · 2026-2

- **Estudiante:** Jhon Bayron Peláez Guerra
- **Docente:** Andrés Felipe Sánchez Aguiar
- **Fecha de elaboración:** 2026-10-10 (revisión de auditoría: 2026-10-10)
- **Fuente primaria:** Especificación Funcional del Negocio: NexusMarket
- **Base:** Entregable 1 (modelo de dominio)

> **Nota de honestidad:** este documento distingue explícitamente lo **implementado y probado**,
> lo **parcial** y lo **pendiente de confirmación**. No se presenta nada como completo si depende
> de información que la fuente no define.

---

## 1. Alcance de la evidencia

La evaluación concertada define esta evidencia como **servicios Spring Boot integrados con el
modelo de dominio** de la primera entrega, que **resuelven casos de uso** y **aplican reglas de
negocio**, con separación de responsabilidades, dependencias apropiadas y código en English.

Este incremento implementa la **capa de aplicación** (casos de uso y servicios). **No** implementa
endpoints REST, persistencia real ni autenticación: corresponden a evidencias posteriores.

---

## 2. Arquitectura

```
com.nexusmarket
├── domain/                      Entidades y reglas de negocio (Entregable 1, sin framework)
├── application/
│   ├── port/in/                 Puertos de entrada: un caso de uso por interfaz
│   ├── port/out/                Puertos de salida: repositorios requeridos por los servicios
│   ├── service/                 Servicios que implementan los casos de uso (@Service)
│   ├── error/                   Excepciones de la capa de aplicación
│   ├── context/                 Actor que ejecuta el caso de uso (ActorContext: identidad + rol)
│   └── support/                 Apoyo transversal: Authorization (rol) y Validation (entradas)
└── infrastructure/
    └── persistence/memory/      Adaptadores en memoria TEMPORALES de los puertos de salida
```

**Principio aplicado:** las reglas que una entidad puede verificar por sí misma permanecen en el
dominio (`Inventory.reserve`, `Inventory.setQuantity`, `Order.changeStatus`) y **no se
reimplementan** en los servicios.

---

## 3. Casos de uso realmente implementados

Los puertos de entrada (`application/port/in/`) declaran **15 operaciones**: **10 casos de uso
numerados** más **5 operaciones de consulta**. Cada operación se contrastó con su servicio y sus
pruebas.

### 3.1 Casos de uso numerados (10)

| # | Caso de uso | Actor (fuente) | Servicio · método | Reglas | Prueba |
|---|---|---|---|---|---|
| CU-03 | Registrar vendedor (incorporación) | Administrador (matriz, RN-04) | `SellerRegistrationService.registerSeller` | RN-04, RN-14, RN-15, RG-03 | `SellerRegistrationServiceTest` |
| CU-04 | Registrar bodega (Marketplace / Vendedor) | Administrador (flujo general, paso 1) | `WarehouseService.registerWarehouse` | OBJ-04, RG-03 | `WarehouseServiceTest` |
| CU-05 | Registrar producto | Vendedor (matriz) | `CatalogService.registerProduct` | RN-06, RG-03 | `CatalogServiceTest` |
| CU-06 | Cambiar estado del producto | Vendedor (matriz) | `CatalogService.changeProductStatus` | OBJ-05, RG-03 | `CatalogServiceTest` |
| CU-07 | Registrar existencias iniciales | Vendedor / Operador Logístico (matriz) | `InventoryService.registerStock` | RN-09, RN-10 | `InventoryServiceTest` |
| CU-08 | Reservar inventario | Vendedor / Operador Logístico (matriz) | `InventoryService.reserveStock` | RN-11, RN-12 | `InventoryServiceTest` |
| CU-09 | Consultar inventario distribuido por producto | Vendedor / Operador Logístico (matriz) | `InventoryService.findStockByProduct` | RN-09 | `InventoryServiceTest` |
| CU-10 | Crear pedido (solo estado inicial Carrito) | Comprador (matriz) | `OrderService.createOrder` | OBJ-07, RG-03 | `OrderServiceTest` |
| CU-11 | Cambiar estado del pedido | Comprador / Vendedor / Operador Logístico (matriz) | `OrderService.changeOrderStatus` | RN-13, RN-16, RG-03 | `OrderServiceTest` |
| CU-15 | Solicitar reembolso | Comprador / Administrador (matriz) | `RefundService.requestRefund` | OBJ-11, RG-03 | `RefundServiceTest` |

### 3.2 Operaciones de consulta (5)

| Operación | Origen en la fuente | Servicio · método | Prueba |
|---|---|---|---|
| Buscar vendedor | consulta de apoyo de CU-03 | `SellerRegistrationService.findSeller` | `SellerRegistrationServiceTest` |
| Listar bodegas de un vendedor | consulta de apoyo de CU-04 | `WarehouseService.findWarehousesBySeller` | `WarehouseServiceTest` |
| Listar catálogo publicado | flujo general, paso 4 (publicación) | `CatalogService.findPublishedProducts` | `CatalogServiceTest` |
| Listar productos propios del vendedor | "administrar *sus* productos" (OBJ-05) | `CatalogService.findOwnProducts` | `CatalogServiceTest` |
| Buscar pedido | consulta de apoyo de CU-11 | `OrderService.findOrder` | **Sin prueba directa** (consulta simple) |

**Total:** 10 casos de uso numerados + 5 consultas = **15 operaciones**, implementadas y publicadas.
La única operación sin prueba directa es `findOrder`; se documenta como tal.

### 3.3 Casos de uso NO implementados

Se verificó la especificación completa (participantes, dominios funcionales, flujo general y matriz
de responsabilidades) para determinar si el actor podía deducirse de otra sección:

**(a) Cuatro casos con actor PENDIENTE** (no definido en ninguna sección de la fuente):

| # | Caso de uso | Hallazgo |
|---|---|---|
| CU-01 | Registrar comprador | "Registro de compradores" es un proceso, pero **ningún actor lo realiza**; no se inventa el autorregistro. |
| CU-02 | Administración de usuarios | OBJ-01 no asigna actor; el Administrador administra vendedores y bodegas, no "usuarios". |
| CU-12 | Facturación | OBJ-09 no asigna actor; además los atributos de la factura son PENDIENTES. |
| CU-14 | Registrar devolución | No hay actor; la matriz solo cubre *reembolsos* (Comprador, Administrador). |

**(b) Un caso con actor IDENTIFICADO pero contenido PENDIENTE:**

| # | Caso de uso | Hallazgo |
|---|---|---|
| CU-16 | Reportes administrativos | El actor es **determinable**: "Supervisor: Perfil de consulta y seguimiento operativo". El **contenido y alcance** de los reportes es PENDIENTE → sigue bloqueado por el contenido, no por el actor. |

Nota sobre CU-14: la devolución es también un tipo de movimiento de inventario (cuya administración
corresponde a Vendedor y Operador Logístico), pero esa asignación sería una **inferencia** y
requiere confirmación del docente. No se implementó.

Ninguno de estos casos se implementó: no se amplió el alcance ni se inventaron permisos.

---

## 4. Servicios y puertos

| Servicio | Puerto de entrada | Puertos de salida |
|---|---|---|
| `SellerRegistrationService` | `RegisterSellerUseCase` | `UserRepository`, `SellerRepository` |
| `WarehouseService` | `ManageWarehousesUseCase` | `WarehouseRepository`, `SellerRepository` |
| `CatalogService` | `ManageCatalogUseCase` | `ProductRepository`, `SellerRepository` |
| `InventoryService` | `ManageInventoryUseCase` | `InventoryRepository`, `ProductRepository`, `WarehouseRepository` |
| `OrderService` | `ManageOrdersUseCase` | `OrderRepository`, `BuyerRepository` |
| `RefundService` | `ManageRefundsUseCase` | `RefundRepository`, `OrderRepository`, `BuyerRepository` |

Adaptadores **temporales** (en memoria): `InMemoryUserRepository`, `InMemorySellerRepository`,
`InMemoryBuyerRepository`, `InMemoryWarehouseRepository`, `InMemoryProductRepository`,
`InMemoryInventoryRepository`, `InMemoryOrderRepository`, `InMemoryRefundRepository`.

---

## 5. Autenticación vs. autorización (estado real de RG-01)

| Concepto | Definición | Estado en este incremento |
|---|---|---|
| **Autenticación** | Verificar *quién* es el usuario (credenciales, sesión, token). | **NO implementada.** |
| **Autorización** | Verificar *qué puede hacer* un actor ya identificado. | **Implementada** por rol. |

Cómo se representa el actor: `ActorContext(userId, role)` es un **objeto de valor** que el
llamador construye. `Authorization.requireAnyRole(actor, roles)` comprueba que el actor exista y
que su rol esté entre los permitidos; los roles permitidos provienen de la matriz de la
especificación.

**Consecuencia honesta:** RG-01 ("toda operación debe ser ejecutada por un usuario autenticado")
está **asumida, no verificada**. Los servicios exigen un `ActorContext` no nulo, pero **confían en
su contenido**: nada comprueba que el `userId` y el `role` declarados sean auténticos. La
autenticación real corresponde a la evidencia de seguridad; no se implementó para no ampliar el
alcance. `UserRepository.findById` existe y podría usarse para esa validación futura.

---

## 6. Alcance real de `OrderService`

**Implementado:**

- `createOrder`: crea un pedido en estado inicial **CART** para el comprador autenticado.
- `changeOrderStatus`: cambia el estado delegando las reglas en `Order.changeStatus` (RN-16, RN-13).
- `findOrder`: consulta un pedido.

**NO se puede resolver todavía (bloqueado por el modelo):**

- agregar/quitar productos del pedido, cantidades y precios (la especificación **no describe los
  ítems del pedido**);
- total del pedido y validación del pago;
- reserva de inventario al confirmar la compra;
- generación de factura y de envío asociados;
- devolución por ítem.

**Conclusión:** el **ciclo de estados** del pedido está implementado y probado *estructuralmente*,
pero el pedido **no es funcionalmente completo**: sin ítems no hay una compra real. El ciclo de
pedidos **no debe presentarse como terminado**.

---

## 7. Decisiones y supuestos (provisionales)

| # | Decisión | Naturaleza | Fuente / motivo |
|---|---|---|---|
| D1 | **Persistencia diferida**; adaptadores **en memoria** en este incremento. | **Decisión técnica provisional** (reversible) | La persistencia **sí está nombrada** en la evidencia de API Web ("persistencia con MySQL y/o MongoDB"), pero la evidencia 2 **no la exige ni la prohíbe**. Diferirla es una decisión propia, no una exclusión documental. |
| D2 | **No adoptar SQL Server.** | Provisional | SQL Server no aparece en la evaluación concertada ni en el microcurrículo; se retiró la configuración previa sin respaldo. |
| D3 | Arquitectura de **puertos y adaptadores**; paquetes `application.{port.in, port.out, service, error}` e `infrastructure.persistence.memory`. | Decisión técnica | Alineada con lo expuesto por el docente; el dominio queda sin anotaciones de framework. |
| D4 | **Autorización por rol** según la matriz; los casos de uso sin actor definido **no se implementan**. | Decisión de alcance | No inventar permisos (regla del proyecto). |
| D5 | **Autenticación diferida**: RG-01 se asume, no se verifica (§5). | Limitación declarada | La seguridad corresponde a una evidencia posterior. |
| D6 | Código en **English** en identificadores y comentarios. | Decisión técnica | Requisito de la evaluación concertada. |

---

## 8. Estado de cumplimiento por requisito de la evidencia

| Requisito / indicador (evaluación concertada) | Evidencia en el repositorio | Estado |
|---|---|---|
| Servicios **Spring Boot** | `application/service/*` con `@Service` | **Completo** |
| **Integrados con el modelo de dominio** | los servicios usan entidades de `domain.*` sin anotaciones de framework | **Completo** |
| Publicado **en GitHub** | `origin/main` (`BayronPG/NexusMarket`) | **Completo** |
| **Resuelven casos de uso** | **10 casos numerados** (CU-03, CU-04, CU-05, CU-06, CU-07, CU-08, CU-09, CU-10, CU-11 y CU-15) **+ 5 consultas** (§3) | **Parcial** (4 casos con actor pendiente y 1 con contenido pendiente, §3.3) |
| **Aplican reglas de negocio** | RN-04, RN-06, RN-09 a RN-16, RN-14, RN-15 + RG-01/RG-03 | **Parcial** (no cubiertas las reglas ligadas a ítems del pedido, variantes y facturación) |
| **Separación de responsabilidades** | puertos/adaptadores; dominio vs. aplicación vs. infraestructura | **Completo** |
| **Abstracciones y dependencias apropiadas** | puertos de salida + inyección por constructor + `ActorContext` | **Completo** |
| **Coherencia con el modelo de dominio** | reglas delegadas al dominio (sin duplicarlas) | **Completo** |
| **Código en English** | identificadores y comentarios del código | **Completo** |
| **RG-01** (usuario autenticado) | `ActorContext` requerido, contenido no verificado | **Parcial** (§5) |

---

## 9. Evidencia de pruebas

`mvn test` → **67 pruebas, 0 fallos** (JUnit 5 + AssertJ) al 2026-10-13.

> La Evidencia 2 se cerró con **49 pruebas**. El incremento de persistencia (Fases 1.1, 1.4 y 1.2,
> sin commitear) añadió 18 pruebas: `JpaMapperTest` (10) y `JpaPersistenceTest` (8).

| Clase de prueba | Pruebas | Reglas cubiertas |
|---|---|---|
| `SellerRegistrationServiceTest` | 8 | RN-04, RN-14, RN-15, RG-03 |
| `WarehouseServiceTest` | 7 | OBJ-04, RG-03 |
| `CatalogServiceTest` | 8 | OBJ-05, RN-06, RG-03 |
| `InventoryServiceTest` | 12 | RN-09, RN-10, RN-11, RN-12 |
| `OrderServiceTest` | 8 | RN-13, RN-16, RG-03 |
| `RefundServiceTest` | 5 | OBJ-11, RG-03 |
| `NexusMarketApplicationTests` | 1 | Arranque del contexto Spring (perfil `test`, H2) |
| `JpaMapperTest` *(incremento de persistencia)* | 10 | Mappers dominio ↔ JPA |
| `JpaPersistenceTest` *(incremento de persistencia)* | 8 | Persistencia H2 de las 8 entidades (`@DataJpaTest`) |
| **Total** | **67** | 0 fallos |

---

## 10. Pendientes y confirmaciones requeridas del docente

1. **Actor de cuatro casos de uso** (CU-01, CU-02, CU-12 y CU-14): no definido en la fuente (§3.3a).
2. **Contenido y alcance de los reportes** (CU-16): el actor es determinable (Supervisor), el contenido no (§3.3b).
3. **Ítems del pedido, cantidades y precios**: bloquean la funcionalidad completa de carrito y pedidos (§6).
4. **Variantes del producto**: naturaleza pendiente.
5. **Persistencia**: elección de MySQL y/o MongoDB (decisión provisional D1/D2; evidencia de API Web).
6. **Autenticación real** (RG-01): evidencia de seguridad posterior (§5).

---

## 11. Incremento de persistencia (en curso, 2026-10-13)

Incremento posterior a esta evidencia, **aún no publicado** en `origin/main`:

| Elemento | Estado |
|---|---|
| Dependencias `spring-boot-starter-data-jpa` + `mysql-connector-j` (runtime) + `h2` (test) | Implementado (sin commitear) |
| Configuración: **MySQL real** por variables de entorno; **H2 solo en el perfil `test`** | Implementado (sin commitear) |
| **8 entidades JPA** separadas del dominio (`infrastructure/persistence/jpa/entity/`) | Implementado (sin commitear) |
| **8 mappers** dominio ↔ JPA (`infrastructure/persistence/jpa/mapper/`) | Implementado (sin commitear) |
| Adaptadores JPA que implementan los 8 puertos de salida | **Pendiente (Fase 1.3)** |
| Borde REST (controllers, DTO, validación, errores, OpenAPI) | **Pendiente (Fase 2)** |
| Asociaciones JPA, restricciones de BD y estrategia definitiva de esquema | **Decisión pendiente** |

El **dominio no se modificó**: las entidades JPA son clases **separadas** y los mappers traducen en
ambos sentidos. Plan completo: [PLAN_API_WEB.md](PLAN_API_WEB.md).

---

*Documento elaborado a partir de la Especificación Funcional del Negocio: NexusMarket y del modelo
de dominio del Entregable 1. Los elementos no respaldados por la fuente se marcan como PENDIENTES
y no se implementan.*
