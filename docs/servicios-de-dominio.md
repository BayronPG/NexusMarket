# Servicios de dominio y/o aplicación — NexusMarket

**Entregable 2 — Evidencia 2: Servicios de dominio y/o aplicación (10 %)**
Construcción de Software 2 · Tecnológico de Antioquia · 2026-2

- **Estudiante:** Jhon Bayron Peláez Guerra
- **Docente:** Andrés Felipe Sánchez Aguiar
- **Fecha de elaboración:** 2026-10-10
- **Fuente primaria:** Especificación Funcional del Negocio: NexusMarket
- **Base:** Entregable 1 (modelo de dominio)

---

## 1. Alcance de la evidencia

La evaluación concertada define esta evidencia como **servicios Spring Boot integrados con el
modelo de dominio** de la primera entrega, que **resuelven casos de uso** y **aplican reglas de
negocio**, con separación de responsabilidades, dependencias apropiadas y código en English.

Este incremento implementa la **capa de aplicación** (casos de uso y servicios). **No** implementa
endpoints REST ni persistencia real: la API Web y la base de datos corresponden a las evidencias
5 y 6, y la selección del motor de base de datos sigue pendiente (D-004).

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
│   ├── ActorContext             Actor autenticado (identidad + rol)
│   ├── Authorization            Verificación de rol (RG-01, RG-03)
│   └── Validation               Verificación de entradas obligatorias
└── infrastructure/
    └── persistence/memory/      Adaptadores en memoria TEMPORALES de los puertos de salida
```

**Principio aplicado:** las reglas que una entidad puede verificar por sí misma permanecen en el
dominio (`Inventory.reserve`, `Inventory.setQuantity`, `Order.changeStatus`) y **no se
reimplementan** en los servicios. Los servicios de aplicación autorizan al actor, validan la
entrada y orquestan el dominio a través de los puertos de salida.

---

## 3. Casos de uso implementados

El actor de cada caso de uso procede de la **matriz de responsabilidades explícita** de la
especificación o del flujo general. Cuando la fuente no define el actor, el caso de uso **no se
implementó** (no se inventa).

| # | Caso de uso | Actor (fuente) | Servicio | Reglas |
|---|---|---|---|---|
| CU-03 | Registrar vendedor (incorporación) | Administrador (matriz, RN-04) | `SellerRegistrationService` | RN-04, RN-14, RN-15, RG-01, RG-03 |
| CU-04 | Registrar bodega (Marketplace / Vendedor) | Administrador (flujo, paso 1) | `WarehouseService` | OBJ-04, RG-03 |
| CU-05 | Registrar producto | Vendedor (matriz) | `CatalogService` | RN-06, RG-03 |
| CU-06 | Cambiar estado del producto | Vendedor (matriz) | `CatalogService` | OBJ-05, RG-03 |
| — | Consultar catálogo publicado | Autenticado | `CatalogService` | RG-01 |
| CU-07 | Registrar existencias iniciales | Vendedor / Operador Logístico (matriz) | `InventoryService` | RN-09, RN-10 |
| CU-08 | Reservar inventario | Vendedor / Operador Logístico (matriz) | `InventoryService` | RN-11, RN-12 |
| CU-09 | Consultar inventario distribuido por producto | Vendedor / Operador Logístico (matriz) | `InventoryService` | RN-09 |
| CU-10/11 | Crear pedido (estado inicial Carrito) | Comprador (matriz) | `OrderService` | OBJ-07, RG-03 |
| CU-11 | Cambiar estado del pedido | Comprador / Vendedor / Operador Logístico (matriz) | `OrderService` | RN-13, RN-16, RG-03 |
| CU-15 | Solicitar reembolso | Comprador / Administrador (matriz) | `RefundService` | OBJ-11, RG-03 |

### Casos de uso NO implementados en este incremento (actor no definido por la fuente)

| Caso de uso | Motivo |
|---|---|
| CU-01 Registrar comprador | La fuente no define quién lo registra (no se inventa el autorregistro). |
| CU-02 Administración de usuarios | La fuente no asigna el actor de la administración de usuarios. |
| CU-12 Facturación | Actor no asignado y atributos de la factura PENDIENTES. |
| CU-14 Registrar devolución | Actor no asignado en la matriz. |
| CU-16 Reportes administrativos | Contenido y alcance de los reportes PENDIENTES. |

---

## 4. Servicios y puertos

### Servicios de aplicación

| Servicio | Puerto de entrada | Puertos de salida |
|---|---|---|
| `SellerRegistrationService` | `RegisterSellerUseCase` | `UserRepository`, `SellerRepository` |
| `WarehouseService` | `ManageWarehousesUseCase` | `WarehouseRepository`, `SellerRepository` |
| `CatalogService` | `ManageCatalogUseCase` | `ProductRepository`, `SellerRepository` |
| `InventoryService` | `ManageInventoryUseCase` | `InventoryRepository`, `ProductRepository`, `WarehouseRepository` |
| `OrderService` | `ManageOrdersUseCase` | `OrderRepository`, `BuyerRepository` |
| `RefundService` | `ManageRefundsUseCase` | `RefundRepository`, `OrderRepository`, `BuyerRepository` |

### Adaptadores (temporales)

`InMemoryUserRepository`, `InMemorySellerRepository`, `InMemoryBuyerRepository`,
`InMemoryWarehouseRepository`, `InMemoryProductRepository`, `InMemoryInventoryRepository`,
`InMemoryOrderRepository`, `InMemoryRefundRepository`.

> Marcados explícitamente como **temporales**: permiten ejecutar y probar los servicios sin
> elegir una base de datos (D-004 pendiente). Un adaptador Spring Data los reemplazará en la
> evidencia de API Web.

---

## 5. Modelo de errores

| Excepción | Significado | Trazabilidad |
|---|---|---|
| `AuthorizationException` | Actor no autenticado o rol no permitido | RG-01, RG-03, RN-04 |
| `BusinessRuleException` | Regla que depende de estado externo a una entidad (unicidad) | RN-14, RN-15 |
| `ValidationException` | Entrada obligatoria ausente o en blanco | OBJ-01 |
| `NotFoundException` | Referencia a un registro inexistente | OBJ-01..OBJ-11 |

Las reglas verificadas por el dominio se manifiestan como `IllegalArgumentException` / `IllegalStateException`
de la propia entidad (RN-10, RN-11, RN-12, RN-13, RN-16), de modo que existe **una única fuente**
para cada regla.

---

## 6. Evidencia de pruebas

`mvn test` → **49 pruebas, 0 fallos** (JUnit 5 + AssertJ).

| Clase de prueba | Pruebas | Reglas cubiertas |
|---|---|---|
| `SellerRegistrationServiceTest` | 8 | RN-04, RN-14, RN-15, RG-01, RG-03 |
| `WarehouseServiceTest` | 7 | OBJ-04, RG-03 |
| `CatalogServiceTest` | 8 | OBJ-05, RN-06, RG-03 |
| `InventoryServiceTest` | 12 | RN-09, RN-10, RN-11, RN-12 |
| `OrderServiceTest` | 8 | RN-13, RN-16, RG-03 |
| `RefundServiceTest` | 5 | OBJ-11, RG-03 |
| `NexusMarketApplicationTests` | 1 | Arranque del contexto Spring |

---

## 7. Pendientes

1. **Persistencia (D-004)**: elegir MySQL y/o MongoDB y sustituir los adaptadores en memoria.
2. **Casos de uso sin actor definido** (CU-01, CU-02, CU-12, CU-14, CU-16): requieren aclaración.
3. **Pendientes del modelo** (variantes, contenido del pedido, estados, reportes): bloquean la
   implementación completa de catálogo, carrito y pedidos.
4. **Seguridad real** (RG-01): el `ActorContext` es el punto de integración previsto.

---

*Documento elaborado a partir de la Especificación Funcional del Negocio: NexusMarket y del
modelo de dominio del Entregable 1. Los elementos no respaldados por la fuente se marcan como
PENDIENTES y no se implementan.*
