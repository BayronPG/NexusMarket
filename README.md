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

### Incremento de persistencia (en curso, sin commitear)

Incremento posterior a E2, **aún no publicado** en `origin/main`:

- **Fases 1.1, 1.4 y 1.2 implementadas**: dependencias de persistencia, configuración de MySQL por
  variables de entorno, perfil `test` con H2, y **8 entidades JPA** + **8 mappers** dominio ↔ JPA.
- **Adaptadores JPA de los repositorios: pendientes** (Fase 1.3).
- **Borde REST: pendiente** (Fase 2).
- **Asociaciones JPA, restricciones de base de datos y estrategia definitiva de esquema: sujetas a decisión.**
- Plan detallado: [docs/PLAN_API_WEB.md](docs/PLAN_API_WEB.md).

## Stack técnico

- **Java 17** (LTS) y **Maven**.
- **Spring Boot 3.4.5** (Web, Data JPA).
- **Arquitectura de puertos y adaptadores**: `domain` · `application` · `infrastructure`.
- **MySQL** como base de datos **real** (configurada por variables de entorno).
- **H2** exclusivamente para **pruebas** (perfil `test`).
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
    │       └── persistence/
    │           ├── memory/        # Adaptadores en memoria (temporales, para pruebas)
    │           └── jpa/
    │               ├── entity/    # 8 entidades JPA (separadas del dominio)
    │               └── mapper/    # 8 mappers dominio <-> JPA
    ├── main/resources/application.properties        # MySQL (variables de entorno)
    ├── test/java/com/nexusmarket/                   # Pruebas
    └── test/resources/application-test.properties   # Perfil de pruebas (H2)
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
(`src/test/resources/application-test.properties`).

## Arquitectura

El proyecto sigue una organización por capas con puertos y adaptadores:

- **Dominio (`domain`)**: entidades y reglas de negocio, sin anotaciones de framework.
- **Aplicación (`application`)**: casos de uso (puertos de entrada), servicios que los implementan y puertos de salida para la persistencia.
- **Infraestructura (`infrastructure`)**: adaptadores que implementan los puertos de salida
  (`persistence/memory` en memoria y `persistence/jpa` con entidades y mappers; los adaptadores JPA
  de repositorio están **pendientes**).

Las reglas de negocio que una entidad puede verificar por sí misma permanecen en el dominio
(`Inventory.reserve`, `Order.changeStatus`); los servicios de aplicación las orquestan y no las
duplican.

## Estado y pendientes (2026-10-13)

- **Base de datos real: MySQL** (D-004 resuelta por el estudiante). **MongoDB no se implementa**.
- **Adaptadores en memoria activos**: los `InMemory*` siguen siendo los **únicos adaptadores activos**
  de los puertos de salida; los adaptadores **JPA están pendientes** (Fase 1.3).
- **Entidades y mappers JPA**: 8 entidades separadas del dominio y 8 mappers dominio ↔ JPA
  (implementados, **sin commitear**).
- **Borde REST pendiente**: controllers, DTO, validación, manejo de errores y OpenAPI (Fase 2).
- **Decisiones provisionales** (no definitivas): relaciones como columnas `Long` (P-1), conservar
  `@ElementCollection` (P-2) y mantener las validaciones en la capa de aplicación (P-3).
  La estrategia definitiva de esquema (`ddl-auto` vs. migraciones) sigue pendiente.
- **Pruebas:** `mvn -B clean test` → **67/67 verdes**.
- **Decisiones, supuestos y alcance real** del incremento: [docs/servicios-de-dominio.md](docs/servicios-de-dominio.md).
- Preguntas abiertas del dominio (variantes, contenido del pedido, catálogo de estados,
  reportes): [docs/modelo-de-dominio.md](docs/modelo-de-dominio.md), sección 11.

## Fuentes oficiales

- Especificación Funcional del Negocio: NexusMarket.
- Formato para la Concertación de la Evaluación de los Aprendizajes (FO-DOC-108).
- Microcurrículo Construcción de Software II 2026-2.
