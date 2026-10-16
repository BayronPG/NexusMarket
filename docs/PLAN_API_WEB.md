# Plan — API Web funcional (Evidencia 5)

> **Proyecto:** NexusMarket · **Asignatura:** Construcción de Software 2 · **Periodo:** 2026-2
> **Estudiante:** Jhon Bayron Peláez Guerra · **Docente:** Andrés Felipe Sánchez Aguiar
> **Fecha del plan:** 2026-10-13
> **Estado:** autorizado por el estudiante el 2026-10-13 y **en ejecución**. Implementado al
> 2026-10-15: Fases **1.1–1.7** y **2.1, 2.2, 2.3 y 2.5**, publicadas en `origin/main`. Ver §12.
>
> **Alcance:** Unidad 2 (persistencia JPA/Hibernate) + Evidencia 5 — API Web funcional (20 %).
> **Fuera de este plan:** seguridad (Unidad 4).

---

## 1. Decisiones fijadas (2026-10-13)

| Tema | Decisión |
|---|---|
| **D-004 — persistencia** | **MySQL únicamente**, con **JPA/Hibernate**. **No** se implementa MongoDB. |
| **B-2 — base de datos de pruebas (2026-10-13)** | **H2 embebida** solo en el perfil `test`; **MySQL** es la base de datos real de ejecución y producción. H2 **no** sustituye ni altera la configuración de MySQL. Ver §11. |
| **Convenciones** | Se proponen en §3 siguiendo la estructura actual; se documentan y verifican contra `CODING_RULES.md` antes de implementarse (§4). |
| **Alcance de casos de uso** | Se exponen **solo** los casos de uso y consultas con actor claro. **Fuera:** CU-01, CU-02, CU-12, CU-14, CU-16 (D-018, D-020). |
| **Seguridad** | **No** se implementa Spring Security en esta evidencia. El `ActorContext` se obtiene con una solución **temporal y documentada** (§5), que **no** es autenticación real (D-019). |
| **Evaluación** | **No** se modifican fechas, porcentajes ni criterios. El conflicto de fuentes solo se documenta (§8). |

> **Nota de honestidad (D-008):** las convenciones de §3 son una **propuesta explícita** derivada de la estructura actual del proyecto y de convenciones generales de Spring Boot. **No** se atribuyen al docente ni a la evaluación concertada, y permanecen sujetas a validación.

---

## 2. Contradicciones y pendientes declarados

1. **Persistencia nombrada en la Evidencia 5, no en la 2 (D-017).** La concertada exige persistencia *MySQL y/o MongoDB* dentro de la Evidencia 5 (API Web). D-004 queda **resuelta a MySQL** por decisión del estudiante.
2. **RG-01 asumida, no verificada (D-019).** Hoy la autorización es por rol, pero la autenticación no existe; `ActorContext` lo construye el llamador. Se mantiene declarado.
3. **Conflicto de estructura de evaluación (D-013).** Concertada vs. microcurrículo. **Solo documentado; sin cambios** (§8).
4. **Consecuencia técnica:** al añadir JPA, la aplicación **deja de arrancar sin datasource configurado**. Se resuelve en 1.4 (configuración) y 1.5 (esquema). Se anticipa como riesgo (§9).

---

## 3. Convenciones propuestas

### 3.1 Paquetes

**Sin cambios (ya existen):**

```
com.nexusmarket.domain.<dominio>                 Entidades y reglas (sin framework)
com.nexusmarket.application.port.in              Puertos de entrada (casos de uso)
com.nexusmarket.application.port.out             Puertos de salida (repositorios)
com.nexusmarket.application.service              Servicios @Service
com.nexusmarket.application.error                Excepciones de aplicación
com.nexusmarket.application.context              ActorContext (actor que ejecuta el caso de uso)
com.nexusmarket.application.support              Authorization y Validation (apoyo transversal)
com.nexusmarket.infrastructure.persistence.memory Adaptadores en memoria (se conservan para pruebas)
```

**Nuevos (propuestos):**

```
com.nexusmarket.infrastructure.persistence.jpa          Adaptadores JPA de los puertos de salida
com.nexusmarket.infrastructure.persistence.jpa.entity   Entidades JPA (separadas del dominio)
com.nexusmarket.infrastructure.persistence.jpa.mapper   Mappers explícitos dominio <-> JPA
com.nexusmarket.infrastructure.web.controller           Controladores REST (@RestController)
com.nexusmarket.infrastructure.web.dto                  DTO de entrada y salida (records)
com.nexusmarket.infrastructure.web.mapper               Mapeo dominio <-> DTO
com.nexusmarket.infrastructure.web.error                Manejo global de errores (@RestControllerAdvice)
```

> **Justificación:** los controladores son **adaptadores de entrada**. Mantenerlos bajo
> `infrastructure` conserva el principio de puertos y adaptadores ya aplicado en la Evidencia 2
> (D-016). La concertada menciona "controladores e infraestructura" como preocupaciones
> distinguibles; si el docente prefiere un paquete de primer nivel (p. ej. `com.nexusmarket.web`),
> el cambio es de bajo costo.

### 3.2 Nombres

| Elemento | Patrón propuesto | Ejemplos |
|---|---|---|
| Controlador | `<Recurso>Controller` | `OrderController`, `CatalogController`, `InventoryController`, `WarehouseController`, `SellerController`, `RefundController` |
| DTO de entrada | `<Recurso>Request` (record) | `CreateOrderRequest`, `ChangeOrderStatusRequest` |
| DTO de salida | `<Recurso>Response` (record) | `OrderResponse` |
| Mapeo web | `<Recurso>WebMapper` (métodos estáticos) | `OrderWebMapper` |
| Adaptador JPA | `<Tecnología><Puerto>` (coherente con `InMemory<Puerto>`) | `JpaOrderRepository` |
| Entidad JPA | `<Dominio>JpaEntity` | `OrderJpaEntity` |
| Manejo de errores | `ApiExceptionHandler` | — |

**Normas transversales propuestas:**
- Todo el código fuente en **English** (CODING_RULES).
- Los controladores dependen **solo de puertos de entrada** (`port.in`), nunca de servicios concretos ni de entidades JPA.
- Las entidades de **dominio** permanecen sin anotaciones de framework; el mapeo dominio ↔ JPA ocurre en `infrastructure`.
- Los **DTO** son `record` inmutables; nunca se exponen entidades de dominio directamente.

### 3.3 Operaciones a exponer (alcance confirmado)

Los **6 puertos de entrada** existentes = **15 operaciones** (10 casos de uso numerados + 5 consultas).
**Quedan fuera** CU-01, CU-02, CU-12, CU-14 y CU-16 por no tener actor/contenido definidos.

---

## 4. Verificación contra `CODING_RULES.md`

Reglas aplicables (CODING_RULES.md) y cómo las cumple la propuesta:

| Regla registrada | Cumplimiento de la propuesta |
|---|---|
| Código fuente en English | Nombres de paquetes, clases, DTO y mappers en inglés. ✅ |
| Separación de responsabilidades | Capas dominio / aplicación / web / persistencia sin mezclarse. ✅ |
| Abstracciones y dependencias apropiadas | Los controladores dependen de interfaces de `port.in`; la persistencia implementa `port.out`. ✅ |
| Coherencia entre dominio, servicios, controladores e infraestructura | El dominio no depende de framework; servicios y controladores solo conocen abstracciones. ✅ |

> **Lo que CODING_RULES.md prohíbe inventar** (nombres de paquetes, sufijos, formato de clases, reglas de
> DTO, anotaciones obligatorias, estructura exacta) se levanta aquí **por decisión explícita del
> estudiante (D-014: el documento oficial no será suministrado)**, no por iniciativa del agente.
> Estas convenciones son **una propuesta revisable**, no una regla oficial.

---

## 5. `ActorContext` temporal (sin seguridad real)

Como **no** se implementa Spring Security en esta evidencia:

- Los controladores obtienen el `ActorContext` mediante un componente **temporal** que lee la
  identidad y el rol desde cabeceras HTTP explícitas (p. ej. `X-Actor-Id`, `X-Actor-Role`).
- Este mecanismo se **documenta** en el README y en `docs/`, marcado como **provisional**.
- **No** se presenta como autenticación: `ActorContext` seguirá sin verificar la identidad del
  llamador (coherente con D-019). La autenticación real corresponde a la evidencia de seguridad.

---

## 6. Plan por fases

### Fase 1 — Persistencia (Unidad 2 · RA-M2)

| # | Tarea | Entregable | Verificación | Depende de |
|---|---|---|---|---|
| 1.1 | Añadir `spring-boot-starter-data-jpa` + driver MySQL al `pom.xml` | `pom.xml` | `mvn compile` | — |
| 1.2 | Entidades JPA con relaciones y validaciones | `...jpa.entity` | `@DataJpaTest` | 1.1 |
| 1.3 | Adaptadores JPA que implementan los **8 puertos de salida** | `...jpa` | `@DataJpaTest` | 1.2 |
| 1.4 | Configuración externa (datasource/JPA) por ambiente | `application*.properties` | arranque | 1.1 |
| 1.5 | Estrategia de esquema (`ddl-auto` o Flyway) | config/migraciones | verificación de esquema | 1.4 |
| 1.6 | Sustituir adaptadores en memoria por JPA (memoria queda para pruebas) | `infrastructure` | pruebas de servicio verdes | 1.3 |
| 1.7 | Pruebas de integración de persistencia | tests | `mvn test` | 1.6 |

### Fase 2 — API REST (Unidad 3 · RA-M3 · Evidencia 5)

| # | Tarea | Entregable | Verificación |
|---|---|---|---|
| 2.1 | Controladores REST sobre los 6 puertos de entrada | `...web.controller` | MockMvc |
| 2.2 | DTO de entrada/salida + mapeo | `...web.dto`, `...web.mapper` | tests de mapeo |
| 2.3 | Validación de entrada (`spring-boot-starter-validation`, `@Valid`) | DTOs validados | tests 400 |
| 2.4 | Manejo global de errores | `...web.error` | tests por código HTTP |
| 2.5 | Resolución temporal del `ActorContext` (§5) | resolver temporal | tests |
| 2.6 | OpenAPI / Swagger UI | dependencia + config | abrir Swagger |
| 2.7 | Colección Postman/Bruno | archivo en repo | ejecución manual |
| 2.8 | Pruebas de integración end-to-end | tests | `mvn test` |

**Mapeo de errores propuesto** (sujeto a confirmación):

| Excepción existente | HTTP propuesto |
|---|---|
| `ValidationException` | 400 Bad Request |
| `AuthorizationException` | 403 Forbidden |
| `NotFoundException` | 404 Not Found |
| `BusinessRuleException` | 409 Conflict |

### Fase 3 — Cierre de la evidencia
Documentación (`README.md`, `docs/`), actualización de `trazabilidad.md` y **propuesta** de commits.

### Fase 4 — Fuera de este plan
**Seguridad real** (Spring Security) = **Unidad 4**, evidencia distinta.

---

## 7. Archivos afectados

```
pom.xml                                    ← dependencias (JPA, driver MySQL; luego validation, OpenAPI)
src/main/resources/application*.properties ← datasource/JPA
src/main/java/com/nexusmarket/
├── application/                           ← sin cambios (puertos y servicios ya existen)
├── domain/                                ← sin cambios
└── infrastructure/
    ├── persistence/memory/                ← se conserva (pruebas)
    ├── persistence/jpa/                   ← NUEVO (Fase 1)
    └── web/                               ← NUEVO (Fase 2)
src/test/java/com/nexusmarket/…            ← pruebas nuevas
docs/ · README.md                          ← documentación de la evidencia
```

---

## 8. Conflicto de evaluación (solo documentado)

- La **evaluación concertada 2026-2** define 6 evidencias (10 %/10 %/20 %/20 %/20 %/20 %, Acuerdo N.° 10/2022).
- El **microcurrículo** define 4 hitos + parcial + final (15 %×4 + 20 % + 20 %, Acuerdo N.° 6/2018).
- **No se resuelve ni se altera nada** (D-013). Requiere confirmación del docente.

---

## 9. Riesgos y bloqueos

| Riesgo | Impacto | Mitigación |
|---|---|---|
| Añadir JPA rompe el arranque sin datasource | La app no inicia tras 1.1 | Perfiles + datasource en 1.4 |
| Pruebas sin BD (hoy en memoria) | Regresiones | Conservar adaptadores en memoria para pruebas |
| Exponer entidades de dominio | Acoplamiento | DTO obligatorios (2.2) |
| Inventar convenciones no validadas | Choque con expectativas del docente | §3–§4 declaradas como propuesta revisable |
| MySQL no disponible en el entorno | Bloqueo de 1.6/1.7 | Definir datasource de prueba (H2/Testcontainers) o instancia local |

---

## 10. Trazabilidad

| Resultado de aprendizaje | Evidencia | Tareas del plan |
|---|---|---|
| RA-M2 (persistencia JPA/Hibernate) | Unidad 2 | Fase 1 (1.1–1.7) |
| RA-M3 (API REST, DTO, validación, errores, OpenAPI, pruebas) | Evidencia 5 (20 %) | Fase 2 (2.1–2.8) |
| RA-M1 (IoC/DI, arquitectura por capas, configuración externa) | Unidad 1 (E2) | Ya cumplido; se conserva |
| RA-M4 (integración, seguridad, entrega) | Unidad 4 | Fase 4 (fuera de este plan) |

---

## 11. Base de datos real vs. base de datos de pruebas (Fase 1.4)

### 11.1 MySQL — base de datos real (ejecución y producción)

- Configurada en `src/main/resources/application.properties`.
- **Credenciales por variables de entorno**, sin valores reales en el repositorio:
  - `DB_URL` (URL JDBC de MySQL)
  - `DB_USER`
  - `DB_PASSWORD`
- El **driver** es `com.mysql:mysql-connector-j` con alcance `runtime`.
- Esquema: `spring.jpa.hibernate.ddl-auto=update` — **estrategia mínima y temporal**;
  la definitiva (migraciones tipo Flyway) se decide en la **Fase 1.5**.

### 11.2 H2 — base de datos de pruebas (solo pruebas)

- Configurada en `src/test/resources/application-test.properties` (nunca se empaqueta con la aplicación).
- Dependencia `com.h2database:h2` con alcance **`test`**.
- El test de contexto `NexusMarketApplicationTests` usa `@ActiveProfiles("test")`.
- Esquema efímero: `spring.jpa.hibernate.ddl-auto=create-drop`.
- **H2 es una herramienta de prueba**: no sustituye ni altera la configuración real de MySQL.

### 11.3 Diferencias de comportamiento H2 ↔ MySQL a vigilar

| Aspecto | H2 (pruebas) | MySQL (real) | Riesgo |
|---|---|---|---|
| Dialecto SQL | `H2Dialect` (autodetectado) | `MySQLDialect` | Diferencias de funciones/tipos no cubiertas por las pruebas |
| Generación de esquema | `create-drop` | `update` | El esquema real no se valida con pruebas |
| Nombres de tablas/columnas | Insensible salvo configuración | Depende de `lower_case_table_names` | Diferencias de mayúsculas en entornos Linux |
| Tipos y precisión | Tipos genéricos | `DECIMAL`, `DATETIME`, `AUTO_INCREMENT` reales | Comportamiento distinto en bordes numéricos y de fecha |
| Transacciones / bloqueos | Simples | Niveles de aislamiento reales | Diferencias con concurrencia |
| `ON DELETE` / FK | Soportado | Soportado con matices de motor (InnoDB) | Restricciones no aplicadas igual |
| Charset / colación | UTF-8 por defecto | Depende de la configuración del servidor | Acentos y ordenamiento distintos |

> **Conclusión:** H2 da un build verde rápido y aislado, pero **no sustituye la verificación
> contra MySQL**. La validación sobre MySQL real es necesaria antes del cierre de la evidencia.

---

## 12. Estado de implementación (2026-10-15)

| Fase | Tarea | Estado |
|---|---|---|
| 1.1 | Dependencias `spring-boot-starter-data-jpa` + `mysql-connector-j` | ✅ Implementado |
| 1.2 | 8 entidades JPA + 8 mappers dominio ↔ JPA | ✅ Implementado |
| 1.3 | Adaptadores JPA de los 8 puertos de salida | ✅ Implementado |
| 1.4 | Configuración de MySQL (variables de entorno) + perfil `test` con H2 | ✅ Implementado |
| 1.5 | Estrategia de esquema: **Flyway** + `ddl-auto=validate` | ✅ Implementado |
| 1.6 | Sustitución de los adaptadores en memoria por los JPA | ✅ Implementado |
| 1.7 | Pruebas de integración de persistencia contra MySQL real | ✅ Implementado (10/10 en MySQL 8.0) |
| 2.1 | Controladores REST sobre los 6 puertos de entrada (15 endpoints) | ✅ Implementado |
| 2.2 | DTO de entrada/salida + mappers web | ✅ Implementado |
| 2.3 | Validación de entrada (`@Valid`, respuestas 400) | ✅ Implementado |
| 2.4 | Manejo global de errores | ⏳ Pendiente |
| 2.5 | Resolución temporal del `ActorContext` (cabeceras HTTP) | ✅ Implementado |
| 2.6 | OpenAPI / Swagger UI | ⏳ Pendiente |
| 2.7 | Colección Postman/Bruno | ⏳ Pendiente |
| 2.8 | Pruebas de integración end-to-end | ⏳ Pendiente |
| 3 | Cierre de la evidencia (documentación y trazabilidad) | 🔄 En curso |

**Pruebas:** `mvn -B clean test` → **153/153 verdes**; integración contra MySQL 8.0 → **10/10**
(excluida del build por defecto mediante la etiqueta `mysql`).

**Publicado:** `origin/main` en `46eaef3` (2026-10-15).

---

## 13. Decisiones provisionales (2026-10-13)

> **Provisionales, NO definitivas.** Se adoptan para esta entrega por simplicidad y defensa ante el
> docente; pueden revisarse si el análisis del dominio o el docente lo requieren.

| ID | Decisión provisional | Justificación |
|---|---|---|
| **P-1** | **Conservar las relaciones como columnas `Long`**; no crear asociaciones JPA sin cardinalidades respaldadas por el modelo. | El dominio modela las vinculaciones por identificador; declarar cardinalidad o propietario sería inventarlos. Solución sencilla y defendible. |
| **P-2** | **Conservar `@ElementCollection`** para `additionalAddresses` (Buyer) y `variants` (Product). | Representa fielmente atributos de tipo lista sin introducir relaciones no respaldadas. |
| **P-3** | **Mantener las validaciones en la capa de aplicación** (RN-14/RN-15); **no** añadir restricciones nuevas de base de datos. | Es donde ya se validan; añadir `unique`/`nullable` sería una decisión no respaldada por la fuente. |

**Sigue pendiente (no resuelto):** la estrategia definitiva de esquema (`ddl-auto` vs. Flyway) — Fase 1.5.
