# Matriz de trazabilidad

Formato: | ID | Fuente | Requisito | Componente | Evidencia | Prueba | Estado |

## Objetivos funcionales (OBJ)

| ID | Fuente | Requisito | Componente | Evidencia | Prueba | Estado |
|---|---|---|---|---|---|---|
| OBJ-01 | Especificación | Administrar información de usuarios | `domain.user.User`, `Role`, `UserStatus` | `src/main/java/com/nexusmarket/domain/user/*.java` | Pendiente | Implementado (clase) |
| OBJ-02 | Especificación | Registro y administración de vendedores | `domain.seller.Seller` | `src/main/java/com/nexusmarket/domain/seller/Seller.java` | Pendiente | Implementado (clase) |
| OBJ-03 | Especificación | Administrar compradores registrados | `domain.buyer.Buyer` | `src/main/java/com/nexusmarket/domain/buyer/Buyer.java` | Pendiente | Implementado (clase) |
| OBJ-04 | Especificación | Controlar información de bodegas | `domain.warehouse.Warehouse`, `WarehouseType` | `src/main/java/com/nexusmarket/domain/warehouse/*.java` | Pendiente | Implementado (clase) |
| OBJ-05 | Especificación | Gestionar catálogo de productos | `domain.catalog.Product`, `ProductType`, `ProductStatus` | `src/main/java/com/nexusmarket/domain/catalog/*.java` | Pendiente | Implementado (clase) |
| OBJ-06 | Especificación | Administrar inventario distribuido | `domain.inventory.Inventory`, `InventoryMovement`, `InventoryMovementType` | `src/main/java/com/nexusmarket/domain/inventory/*.java` | Pendiente | Implementado (clase) |
| OBJ-07 | Especificación | Gestionar carrito de compras | `domain.order.Cart` | `src/main/java/com/nexusmarket/domain/order/Cart.java` | Pendiente | Implementado (clase) |
| OBJ-08 | Especificación | Controlar ciclo completo de pedidos | `domain.order.Order`, `OrderStatus` | `src/main/java/com/nexusmarket/domain/order/Order.java`, `OrderStatus.java` | Pendiente | Implementado (clase) |
| OBJ-09 | Especificación | Administrar facturación de compras | `domain.billing.Invoice` | `src/main/java/com/nexusmarket/domain/billing/Invoice.java` | Pendiente | Implementado (clase) |
| OBJ-10 | Especificación | Gestionar procesos logísticos | `domain.logistics.Shipment` | `src/main/java/com/nexusmarket/domain/logistics/Shipment.java` | Pendiente | Implementado (clase) |
| OBJ-11 | Especificación | Administrar devoluciones y reembolsos | `domain.returns.Return`, `Refund` | `src/main/java/com/nexusmarket/domain/returns/*.java` | Pendiente | Implementado (clase) |
| OBJ-12 | Especificación | Consolidar información administrativa | `domain.reporting.AdministrativeReport` | `src/main/java/com/nexusmarket/domain/reporting/AdministrativeReport.java` | Pendiente | Implementado (clase) |

## Restricciones generales (RG)

| ID | Fuente | Requisito | Componente | Evidencia | Prueba | Estado |
|---|---|---|---|---|---|---|
| RG-01 | Especificación | Toda operación por usuario autenticado | Transversal (infraestructura/seguridad) | Pendiente | Pendiente | Pendiente (Evidencia 2+) |
| RG-02 | Especificación | Un único rol por usuario | `domain.user.Role`, `User.role` | `src/main/java/com/nexusmarket/domain/user/*.java` | Pendiente | Implementado (clase) |
| RG-03 | Especificación | No administrar información fuera del rol | Reglas de dominio (Buyer, Seller) | Documentado en Javadoc | Pendiente | Parcial (validación en Evidencia 2) |

## Validaciones críticas

| ID | Fuente | Requisito | Componente | Evidencia | Prueba | Estado |
|---|---|---|---|---|---|---|
| VC-01 | Especificación | No reservar inventario inexistente (RN-11) | `Inventory.reserve()` | `Inventory.java` | Pendiente | Implementado (método) |
| VC-02 | Especificación | No reservar inventario Dañado (RN-12) | `Inventory.reserve()` | `Inventory.java` | Pendiente | Implementado (método) |
| VC-03 | Especificación | No modificar pedido finalizado (RN-13) | `Order.changeStatus()` | `Order.java` | Pendiente | Implementado (método) |
| VC-04 | Especificación | Documento de identidad único (RN-14) | `User.identityDocument` | `User.java` | Pendiente | Implementado (atributo; unicidad en Evidencia 2) |
| VC-05 | Especificación | Correo electrónico único (RN-15) | `User.email` | `User.java` | Pendiente | Implementado (atributo; unicidad en Evidencia 2) |
