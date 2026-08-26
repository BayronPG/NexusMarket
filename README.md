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
| **E1 — Modelo de dominio del negocio** | [docs/modelo-de-dominio.md](docs/modelo-de-dominio.md) + clases de dominio en `src/` | 2026-08-30 |

## Estructura del repositorio

```
NexusMarket/
├── README.md                      # Este archivo
├── pom.xml                        # Proyecto Maven (Java 21)
├── docs/
│   └── modelo-de-dominio.md       # Entregable 1: modelo de dominio documentado
└── src/main/java/com/nexusmarket/domain/
    ├── user/        # OBJ-01 Usuarios
    ├── buyer/       # OBJ-03 Compradores
    ├── seller/      # OBJ-02 Vendedores
    ├── warehouse/   # OBJ-04 Bodegas
    ├── catalog/     # OBJ-05 Catálogo
    ├── inventory/   # OBJ-06 Inventario
    ├── order/       # OBJ-07 Carrito y OBJ-08 Pedidos
    ├── billing/     # OBJ-09 Facturación
    ├── logistics/   # OBJ-10 Logística
    ├── returns/     # OBJ-11 Devoluciones y reembolsos
    └── reporting/   # OBJ-12 Reportes
```

## Fuentes oficiales

- Especificación Funcional del Negocio: NexusMarket.
- Formato para la Concertación de la Evaluación de los Aprendizajes (FO-DOC-108).
- Microcurrículo Construcción de Software II 2026-2.
