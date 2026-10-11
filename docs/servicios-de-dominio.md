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
│   ├── ActorContext             Actor que ejecuta el caso de uso (identidad + rol)
│   ├── Authorization            Verificación de rol
│   └── Validation               Verificación de entradas obligatorias
└── infrastructure/
    └── persistence/memory/      Adaptadores en memoria TEMPORALES de los puertos de salida
```

**Principio aplicado:** las reglas que una entidad puede verificar por sí misma permanecen en el
dominio (`Inventory.reserve`, `Inventory.setQuantity`, `Order.changeStatus`) y **no se
reimplementan** en los servicios.

---

## 3. Casos de uso implementados

| # | Caso de uso | Actor (fuente) | Servicio | Reglas |
|---|---|---|---|---|
| CU-03 | Registrar vendedor (incorporación) | Administrador (matriz, RN-04) | `SellerRegistrationService` | RN-04, RN-14, RN-15, RG-03 |
| CU-04 | Registrar bodega (Marketplace / Vendedor) | Administrador (flujo, paso 1) | `WarehouseService` | OBJ-04, RG-03 |
| CU-05 | Registrar producto | Vendedor (matriz) | `CatalogService` | RN-06, RG-03 |
| CU-06 | Cambiar estado del producto | Vendedor (matriz) | `CatalogService` | OBJ-05, RG-03 |
| — | Consultar catálogo publicado | Autenticado | `CatalogService` | RG-01 |
| CU-07 | Registrar existencias iniciales | Vendedor / Operador Logístico (matriz) | `InventoryService` | RN-09, RN-10 |
| CU-08 | Reservar inventario | Vendedor / Operador Logístico (matriz) | `InventoryService` | RN-11, RN-12 |
| CU-09 | Consultar inventario distribuido por producto | Vendedor / Operador Logístico (matriz) | `InventoryService` | RN-09 |
| CU-10 | Crear pedido (solo estado inicial Carrito) | Comprador (matriz) | `OrderService` | OBJ-07, RG-03 |
| CU-11 | Cambiar estado del pedido | Comprador / Vendedor / Operador Logístico (matriz) | `OrderService` | RN-13, RN-16, RG-03 |
| CU-15 | Solicitar reembolso | Comprador / Administrador (matriz) | `RefundService` | OBJ-11, RG-03 |

### 3.1 Casos de uso NO implementados

Se verificó la especificación completa (secciones de participantes, dominios funcionales, flujo
general y matriz de responsabilidades) para determinar si el actor ausente en la matriz podía
deducirse de otra sección:

| Caso de uso | Actor en la fuente | Conclusión |
|---|---|---|
| CU-01 Registrar comprador | **No definido** en ninguna sección | Bloqueado: no se inventa el autorregistro ni otro responsable. |
| CU-02 Administración de usuarios | **No definido** (OBJ-01 no asigna actor; el Administrador administra vendedores y bodegas, no "usuarios") | Bloqueado. |
| CU-12 Facturación | **No definido** | Bloqueado. Además los atributos de la factura son PENDIENTES. |
| CU-14 Registrar devolución | **No definido** para devoluciones; la matriz solo cubre *reembolsos* (Comprador, Administrador) | Bloqueado. La devolución es también un tipo de movimiento de inventario (administrado por Vendedor y Operador Logístico), pero esa asignación sería una **inferencia** y requiere confirmación. |
| CU-16 Reportes administrativos | **Determinable**: "Supervisor: Perfil de consulta y seguimiento operativo" | El actor *puede* ser el Supervisor, pero el **contenido y alcance** de los reportes son PENDIENTES → sigue bloqueado por el contenido, no por el actor. |

Ninguna de estas determinaciones se implementó: se dejan para confirmación del docente.

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
autenticación real (y la carga del usuario desde un puerto para validar su rol) corresponde a la
evidencia de seguridad; no se implementó para no ampliar el alcance. `UserRepository.findById`
existe y podría usarse para esa validación futura.

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
pero el pedido **no es funcionalmente completo**: sin ítems no hay una compra real. No debe
presentarse el ciclo de pedidos como terminado.

---

## 7. Decisiones y supuestos (provisionales)

| # | Decisión | Naturaleza | Fuente / motivo |
|---|---|---|---|
| D1 | **Persistencia diferida**; adaptadores **en memoria** en este incremento. | **Decisión técnica provisional** (reversible) | La persistencia **sí está nombrada** en la evidencia de API Web ("persistencia con MySQL y/o MongoDB"), pero la evidencia 2 **no la exige ni la prohíbe**. Diferirla es una decisión propia, no una exclusión documental. |
| D2 | **No adoptar SQL Server.** | Provisional | SQL Server no aparece en la evaluación concertada ni en el microcurrículo; se retiró la configuración previa sin respaldo. |
| D3 | Arquitectura de **puertos y adaptadores**; paquetes `application.{port.in, port.out, service, error}` e `infrastructure.persistence.memory`. | Decisión técnica | Alineada con lo expuesto por el docente; el dominio queda sin anotaciones de framework. |
| D4 | **Autorización por rol** según la matriz; los casos de uso sin actor definido **no se implementan**. | Decisión de alcance | No inventar permisos (regla del proyecto). |
| D5 | **Autenticación diferida**: RG-01 se asume, no se verifica (§5). | Limitación declarada | La seguridad corresponde a una evidencia posterior. |
| D6 | Código en **English**; se aplican las reglas disponibles. | Decisión de codificación | El documento oficial "Regla de Codificación" no fue suministrado; no se inventan reglas. |

---

## 8. Estado de cumplimiento por requisito de la evidencia

| Requisito / indicador (evaluación concertada) | Evidencia en el repositorio | Estado |
|---|---|---|
| Servicios **Spring Boot** | `application/service/*` con `@Service` | **Completo** |
| **Integrados con el modelo de dominio** | los servicios usan entidades de `domain.*` sin anotaciones de framework | **Completo** |
| Publicado **en GitHub** | `origin/main` (`BayronPG/NexusMarket`) | **Completo** |
| **Resuelven casos de uso** | 7 casos (CU-03 a CU-11, CU-15) | **Parcial** (5 casos sin actor definido) |
| **Aplican reglas de negocio** | RN-04, RN-06, RN-09 a RN-16, RN-14, RN-15 + RG-01/RG-03 | **Parcial** (no cubiertas las reglas ligadas a ítems del pedido, variantes y facturación) |
| **Separación de responsabilidades** | puertos/adaptadores; dominio vs. aplicación vs. infraestructura | **Completo** |
| **Abstracciones y dependencias apropiadas** | puertos de salida + inyección por constructor + `ActorContext` | **Completo** |
| **Coherencia con el modelo de dominio** | reglas delegadas al dominio (sin duplicarlas) | **Completo** |
| **Código en English** | identificadores y comentarios del código | **Completo** |
| **Convenciones de la Regla de Codificación** | documento oficial no suministrado; se aplican las de `CODING_RULES` | **Parcial** |
| **RG-01** (usuario autenticado) | `ActorContext` requerido, contenido no verificado | **Parcial** (§5) |

---

## 9. Evidencia de pruebas

`mvn test` → **49 pruebas, 0 fallos** (JUnit 5 + AssertJ).

| Clase de prueba | Pruebas | Reglas cubiertas |
|---|---|---|
| `SellerRegistrationServiceTest` | 8 | RN-04, RN-14, RN-15, RG-03 |
| `WarehouseServiceTest` | 7 | OBJ-04, RG-03 |
| `CatalogServiceTest` | 8 | OBJ-05, RN-06, RG-03 |
| `InventoryServiceTest` | 12 | RN-09, RN-10, RN-11, RN-12 |
| `OrderServiceTest` | 8 | RN-13, RN-16, RG-03 |
| `RefundServiceTest` | 5 | OBJ-11, RG-03 |
| `NexusMarketApplicationTests` | 1 | Arranque del contexto Spring |

---

## 10. Pendientes y confirmaciones requeridas del docente

1. **Actores de CU-01, CU-02, CU-12 y CU-14** (§3.1): no están definidos en la especificación.
2. **Contenido de los reportes** (CU-16): el actor es determinable (Supervisor), el contenido no.
3. **Ítems del pedido, cantidades y precios**: bloquean la funcionalidad completa de carrito y pedidos (§6).
4. **Variantes del producto**: naturaleza pendiente.
5. **Persistencia**: elección de MySQL y/o MongoDB (decisión provisional D1/D2).
6. **Autenticación real** (RG-01): evidencia de seguridad posterior (§5).
7. **Regla de Codificación**: documento oficial no suministrado.

---

*Documento elaborado a partir de la Especificación Funcional del Negocio: NexusMarket y del modelo
de dominio del Entregable 1. Los elementos no respaldados por la fuente se marcan como PENDIENTES
y no se implementan.*
