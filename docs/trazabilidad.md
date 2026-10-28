# Matriz de trazabilidad

Formato: | ID | Fuente | Requisito | Componente | Evidencia | Prueba | Estado |

## Objetivos funcionales (OBJ)

| ID | Fuente | Requisito | Componente | Evidencia | Prueba | Estado |
|---|---|---|---|---|---|---|
| OBJ-01 | Especificación | Administrar información de usuarios | `domain.user.User`, `Role`, `UserStatus` | `src/main/java/com/nexusmarket/domain/user/*.java` | Pendiente | Implementado (clase); administración sin actor definido |
| OBJ-02 | Especificación | Registro y administración de vendedores | `domain.seller.Seller`, `application.service.SellerRegistrationService` | `SellerRegistrationService.java` | `SellerRegistrationServiceTest` | Implementado y probado |
| OBJ-03 | Especificación | Administrar compradores registrados | `domain.buyer.Buyer`, `application.port.out.BuyerRepository` | `domain/buyer/Buyer.java` | Pendiente | Parcial (actor no definido) |
| OBJ-04 | Especificación | Controlar información de bodegas | `domain.warehouse.Warehouse`, `application.service.WarehouseService` | `WarehouseService.java` | `WarehouseServiceTest` | Implementado y probado |
| OBJ-05 | Especificación | Gestionar catálogo de productos | `domain.catalog.Product`, `application.service.CatalogService` | `CatalogService.java` | `CatalogServiceTest` | Implementado y probado |
| OBJ-06 | Especificación | Administrar inventario distribuido | `domain.inventory.Inventory`, `application.service.InventoryService` | `InventoryService.java` | `InventoryServiceTest` | Implementado y probado |
| OBJ-07 | Especificación | Gestionar carrito de compras | `domain.order.Cart`, `application.service.OrderService` | `OrderService.java` | `OrderServiceTest` | Parcial (contenido del pedido PENDIENTE) |
| OBJ-08 | Especificación | Controlar ciclo completo de pedidos | `domain.order.Order`, `OrderStatus`, `application.service.OrderService` | `OrderService.java` | `OrderServiceTest` | Implementado y probado |
| OBJ-09 | Especificación | Administrar facturación de compras | `domain.billing.Invoice` | `domain/billing/Invoice.java` | Pendiente | Pendiente (actor y atributos no definidos) |
| OBJ-10 | Especificación | Gestionar procesos logísticos | `domain.logistics.Shipment` | `domain/logistics/Shipment.java` | Pendiente | Pendiente (atributos no definidos) |
| OBJ-11 | Especificación | Administrar devoluciones y reembolsos | `domain.returns.Refund`, `application.service.RefundService` | `RefundService.java` | `RefundServiceTest` | Parcial (reembolsos implementados; devoluciones sin actor) |
| OBJ-12 | Especificación | Consolidar información administrativa | `domain.reporting.AdministrativeReport` | `domain/reporting/AdministrativeReport.java` | Pendiente | Pendiente (contenido no definido) |

## Restricciones generales (RG)

| ID | Fuente | Requisito | Componente | Evidencia | Prueba | Estado |
|---|---|---|---|---|---|---|
| RG-01 | Especificación | Toda operación por usuario autenticado | `application.context.ActorContext`, `application.support.Authorization`, `infrastructure.web.ActorContextArgumentResolver` | `ActorContext.java`, `Authorization.java` | Pruebas de rol en cada servicio; `ActorContextArgumentResolverTest` | Implementado (contexto de actor provisional por cabeceras; seguridad real posterior) |
| RG-02 | Especificación | Un único rol por usuario | `domain.user.Role`, `User.role`, `ActorContext.role` | `domain/user/*.java` | `SellerRegistrationServiceTest` | Implementado y probado |
| RG-03 | Especificación | No administrar información fuera del rol | `application.support.Authorization` + servicios | `Authorization.java` | Pruebas de permisos por servicio | Implementado y probado |

## Validaciones críticas

| ID | Fuente | Requisito | Componente | Evidencia | Prueba | Estado |
|---|---|---|---|---|---|---|
| VC-01 | Especificación | No reservar inventario inexistente (RN-11) | `Inventory.reserve()`, `InventoryService.reserveStock` | `Inventory.java`, `InventoryService.java` | `InventoryServiceTest.nonexistentStockCannotBeReserved` | Implementado y probado |
| VC-02 | Especificación | No reservar inventario Dañado (RN-12) | `Inventory.reserve()`, `InventoryService.reserveStock` | `Inventory.java` | `InventoryServiceTest.damagedStockCannotBeReserved` | Implementado y probado |
| VC-03 | Especificación | No modificar pedido finalizado (RN-13) | `Order.changeStatus()`, `OrderService.changeOrderStatus` | `Order.java` | `OrderServiceTest.aFinalizedOrderCannotBeModified` | Implementado y probado |
| VC-04 | Especificación | Documento de identidad único (RN-14) | `User.identityDocument`, `SellerRegistrationService` | `SellerRegistrationService.java` | `SellerRegistrationServiceTest.duplicatedIdentityDocumentIsRejected` | Implementado y probado |
| VC-05 | Especificación | Correo electrónico único (RN-15) | `User.email`, `SellerRegistrationService` | `SellerRegistrationService.java` | `SellerRegistrationServiceTest.duplicatedEmailIsRejected` | Implementado y probado |

## Casos de uso implementados (Entregable 2)

| ID | Fuente | Caso de uso | Componente | Evidencia | Prueba | Estado |
|---|---|---|---|---|---|---|
| CU-03 | Matriz, RN-04 | Registrar vendedor | `SellerRegistrationService` | `application/service/SellerRegistrationService.java` | `SellerRegistrationServiceTest` | Implementado y probado |
| CU-04 | Flujo paso 1 | Registrar bodega | `WarehouseService` | `application/service/WarehouseService.java` | `WarehouseServiceTest` | Implementado y probado |
| CU-05/06 | Matriz, OBJ-05 | Registrar producto / cambiar estado | `CatalogService` | `application/service/CatalogService.java` | `CatalogServiceTest` | Implementado y probado |
| CU-07/08/09 | Matriz, OBJ-06 | Existencias, reserva y consulta distribuida | `InventoryService` | `application/service/InventoryService.java` | `InventoryServiceTest` | Implementado y probado |
| CU-10/11 | Matriz, OBJ-07/08 | Crear pedido y cambiar estado | `OrderService` | `application/service/OrderService.java` | `OrderServiceTest` | Implementado y probado |
| CU-15 | Matriz, OBJ-11 | Solicitar reembolso | `RefundService` | `application/service/RefundService.java` | `RefundServiceTest` | Implementado y probado |

## Persistencia y API Web (2026-10-15)

| Componente | Ubicación | Prueba | Estado |
|---|---|---|---|
| Entidades JPA (8) | `infrastructure/persistence/jpa/entity/` | `JpaPersistenceTest` | Implementado y probado |
| Mappers dominio ↔ JPA (8) | `infrastructure/persistence/jpa/mapper/` | `JpaMapperTest` | Implementado y probado |
| Repositorios Spring Data (8) | `infrastructure/persistence/jpa/repository/` | `Jpa*RepositoryAdapterTest` | Implementado y probado |
| Adaptadores JPA de los puertos de salida (8) | `infrastructure/persistence/jpa/adapter/` | `Jpa*RepositoryAdapterTest` | Implementado y probado |
| Sustitución memoria ↔ JPA | perfil `memory` vs. por defecto | `JpaAdapterWiringTest`, `MemoryAdapterWiringTest` | Implementado y probado |
| Esquema con Flyway (v1) | `src/main/resources/db/migration/` | `PersistenceMysqlIntegrationTest` | Implementado; validado en MySQL 8.0 |
| Controladores REST (6; 15 endpoints) | `infrastructure/web/controller/` | `*ControllerTest` (MockMvc) | Implementado y probado |
| DTO y mappers web | `infrastructure/web/dto/`, `infrastructure/web/mapper/` | `WebMapperTest` | Implementado y probado |
| Validación de entrada (`@Valid` → 400) | DTO de entrada | `*ControllerTest` | Implementado y probado |
| `ActorContext` temporal por cabeceras | `infrastructure/web/ActorContextArgumentResolver.java` | `ActorContextArgumentResolverTest` | Implementado y probado (provisional; no es autenticación) |
| Manejo global de errores (`@RestControllerAdvice`) | `infrastructure/web/error/` | `GlobalExceptionHandlerTest` | Implementado y probado (400/403/404/409/500) |
| OpenAPI, Postman, E2E | — | — | **Pendiente (Fases 2.6, 2.7, 2.8)** |
| Asociaciones y restricciones de esquema | — | — | Relaciones como columnas `Long` (P-1); esquema versionado con Flyway |
