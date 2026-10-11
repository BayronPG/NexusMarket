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

## Estructura del repositorio

```
NexusMarket/
├── README.md
├── pom.xml
├── docs/
│   ├── modelo-de-dominio.md       # Entregable 1
│   ├── servicios-de-dominio.md    # Entregable 2
│   └── trazabilidad.md            # Matriz de trazabilidad
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
    │       └── persistence/memory/# Adaptadores en memoria (temporales)
    └── test/java/com/nexusmarket/ # Pruebas de los casos de uso
```

## Cómo ejecutar

Requisitos: JDK 17 y Maven 3.9+.

```powershell
mvn test               # compila el proyecto y ejecuta las pruebas
mvn spring-boot:run    # inicia la aplicación Spring Boot
```

## Arquitectura

El proyecto sigue una organización por capas con puertos y adaptadores:

- **Dominio (`domain`)**: entidades y reglas de negocio, sin anotaciones de framework.
- **Aplicación (`application`)**: casos de uso (puertos de entrada), servicios que los implementan y puertos de salida para la persistencia.
- **Infraestructura (`infrastructure`)**: adaptadores que implementan los puertos de salida.

Las reglas de negocio que una entidad puede verificar por sí misma permanecen en el dominio
(`Inventory.reserve`, `Order.changeStatus`); los servicios de aplicación las orquestan y no las
duplican.

## Estado y pendientes

- **Persistencia: pendiente (D-004).** Este incremento usa adaptadores **en memoria** (temporales)
  para poder ejecutar y probar los servicios sin decidir una base de datos. La evaluación
  concertada admite **MySQL y/o MongoDB**; la selección se hará en la evidencia de API Web.
- Endpoints REST, seguridad y contratos OpenAPI: evidencias posteriores.
- **Decisiones, supuestos y alcance real** del incremento (naturaleza provisional de la
  persistencia, diferencia entre autenticación y autorización, alcance de `OrderService` y
  casos de uso bloqueados): [docs/servicios-de-dominio.md](docs/servicios-de-dominio.md).
- Preguntas abiertas del dominio (variantes, contenido del pedido, catálogo de estados,
  reportes) documentadas en [docs/modelo-de-dominio.md](docs/modelo-de-dominio.md), sección 11.

## Fuentes oficiales

- Especificación Funcional del Negocio: NexusMarket.
- Formato para la Concertación de la Evaluación de los Aprendizajes (FO-DOC-108).
- Microcurrículo Construcción de Software II 2026-2.
