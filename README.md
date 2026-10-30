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
- **Esquema con Flyway** (`V1__initial_schema.sql`) y `spring.jpa.hibernate.ddl-auto=validate`.
- **Borde REST (Fases 2.1–2.4 y 2.5)**: 6 controladores sobre los 6 puertos de entrada
  (**15 endpoints**), DTO y mappers web, validación `@Valid` (400), manejo global de errores con
  respuesta uniforme y resolución temporal del `ActorContext` por cabeceras.
- **OpenAPI (2.6)**: los 15 endpoints documentados con `@Tag`, `@Operation`, `@ApiResponse` y las
  cabeceras `X-Actor-Id` / `X-Actor-Role`; `/v3/api-docs` y Swagger UI verificados con `OpenApiDocsTest`.
- **Postman (2.7)**: `docs/postman/NexusMarket.postman_collection.json` (6 carpetas, 15 requests).
- **Pendiente**: pruebas end-to-end (2.8).
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

## Estado y pendientes (2026-10-15)

- **Base de datos real: MySQL** (D-004 resuelta por el estudiante). **MongoDB no se implementa**.
- **Persistencia cerrada (Fases 1.1–1.7)**: los adaptadores **JPA son la implementación por defecto**
  de los 8 puertos de salida; los `InMemory*` quedan **solo para pruebas** bajo el perfil `memory`.
- **Esquema**: lo crea **Flyway** (`V1__initial_schema.sql`); Hibernate solo valida
  (`spring.jpa.hibernate.ddl-auto=validate`).
- **Borde REST (Fases 2.1–2.4 y 2.5)**: 6 controladores, 15 endpoints, DTO y mappers web, validación
  de entrada (`@Valid` → 400), manejo global de errores (`@RestControllerAdvice` → 400/403/404/409/500)
  y `ActorContext` provisional por cabeceras `X-Actor-Id` / `X-Actor-Role` (**no es autenticación**;
  la seguridad real es la Unidad 4).
- **Evidencia 5**: 2.6 (OpenAPI de los 15 endpoints), 2.7 (colección Postman), 2.8 (pruebas E2E de
  los tres flujos reales) y 2.9 (paginación de los cuatro listados) resueltas.
- **Paginación (2.9)**: los listados aceptan `page` (≥0), `size` (1-100, por defecto 20) y
  `sort` (`campo` o `campo,asc|desc`); responden `content`, `page`, `size`, `totalElements` y
  `totalPages`. Filtros opcionales: `sellerId` y `status` en productos, `warehouseId` en inventario.
- **Calidad**: JaCoCo mide en la fase `test` y `mvn verify` exige ≥ 80 % líneas y ≥ 65 % ramas
  (actual: 86.0 % líneas y 70.8 % ramas).
- **Decisiones provisionales** (no definitivas): relaciones como columnas `Long` (P-1), conservar
  `@ElementCollection` (P-2) y mantener las validaciones de negocio en la capa de aplicación (P-3).
- **Pruebas:** `mvn -B clean test` → **211/211 verdes**; integración contra MySQL 8.0 → **10/10**.
- **Decisiones, supuestos y alcance real** del incremento: [docs/servicios-de-dominio.md](docs/servicios-de-dominio.md).
- Preguntas abiertas del dominio (variantes, contenido del pedido, catálogo de estados,
  reportes): [docs/modelo-de-dominio.md](docs/modelo-de-dominio.md), sección 11.

## Fuentes oficiales

- Especificación Funcional del Negocio: NexusMarket.
- Formato para la Concertación de la Evaluación de los Aprendizajes (FO-DOC-108).
- Microcurrículo Construcción de Software II 2026-2.
