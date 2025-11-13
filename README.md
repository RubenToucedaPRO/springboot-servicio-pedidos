# SpringBoot-Servicio-Pedidos

Microservicio de ejemplo para la gestión de pedidos implementado con **Java 21** y **Spring Boot 3.2**, siguiendo **Clean Architecture**.

---

## Descripción

Este proyecto implementa un microservicio para crear, modificar, consultar y eliminar pedidos, con las siguientes características:

- **API REST** para operaciones CRUD sobre pedidos.
- **Modelo de dominio inmutable** con Value Objects (`Money`, `Quantity`, `ProductId`, `OrderId`).
- **Adaptadores de persistencia**:
  - H2 en memoria (`H2OrderRepository`) para desarrollo rápido y tests.
  - JPA / Spring Data con Postgres (`JpaOrderRepository` + adapter) para producción.
- **Soporte para eventos en memoria** (event bus) para pruebas y debugging.
- Scripts de migración SQL y ejemplo de **Docker Compose** para Postgres.

**Objetivo:** servir como ejemplo de cómo estructurar una aplicación Java/Spring Boot con límites claros entre dominio y dependencias, facilitar pruebas y permitir cambiar la tecnología de persistencia sin modificar los casos de uso.

---

## Estructura del Proyecto (Paquetes)

```
src/main/java/com/pedidos/
│
├── domain/                 # Núcleo de negocio
│   ├── entities/           # Entidades con identidad y comportamiento (ej: Order)
│   ├── valueobjects/       # Value Objects inmutables y validaciones (Money, Quantity...)
│   ├── events/             # Eventos del dominio
│   └── errors/             # Excepciones y errores del dominio
│
├── application/            # Casos de uso, DTOs y puertos
│   ├── usecase/            # Casos de uso (ej: CreateOrderUseCase)
│   ├── dto/                # DTOs de la capa de aplicación
│   └── port/
│       ├── in/             # Interfaces de entrada (use cases)
│       └── out/            # Interfaces de salida (repositorios, APIs externas)
│
├── infrastructure/         # Implementaciones tecnológicas (adaptadores)
│   ├── adapter/            # Contiene adaptadores (implementaciones concretas de puertos)
│   │   └── persistence/    # Adaptadores de persistencia agrupados por tecnología
│   │       ├── entity/     # Entidades JPA (OrderEntity, OrderItemEntity)
│   │       ├── jpa/        # Adaptador JPA: Spring Data repo + adapter
│   │       │   ├── JpaOrderRepository.java
│   │       │   └── SpringDataOrderRepositoryAdapter.java
│   │       └── h2/         # Adaptador H2/JDBC: H2OrderRepository
│   ├── rest/               # Controladores REST, DTOs y mapeadores
│   ├── configuration/      # Beans, wiring, propiedades
│   └── external/           # Integraciones externas (APIs, colas, email)
│
├── shared/                 # Excepciones, utilidades, constantes compartidas
└── resources/              # `src/main/resources` (application.yml, migrations, etc.)

// Tests:
src/test/java/com/pedidos/{domain,application,infrastructure}  # tests unitarios e integración

```

## Requisitos

- **Java 21 (LTS)**
- **Maven**
- Opcional: Docker para levantar Postgres y pgAdmin localmente

---


## Comandos Maven útiles

| Acción | Comando |
|--------|---------|
| Limpiar proyecto | `mvn clean` |
| Compilar y empaquetar | `mvn clean package` |
| Compilar sin tests | `mvn clean package -DskipTests` |
| Ejecutar aplicación | `mvn spring-boot:run` <br> `java -jar target/pedidos-0.0.1-SNAPSHOT.jar` |
| Ejecutar tests | `mvn test` |
| Ejecutar test específico | `mvn -Dtest=NombreClaseTest test` |
| Ejecutar con perfil Spring | `mvn -Dspring-boot.run.profiles=dev spring-boot:run` <br> `java -Dspring.profiles.active=dev -jar target/pedidos-0.0.1-SNAPSHOT.jar` |

---

## Modos de ejecución

### Desarrollo (`dev`)

- `.env` opcional para Postgres:

DB_KIND=POSTGRES  
DB_URL=jdbc:postgresql://localhost:5432/pedidos  
DB_USER=postgres  
DB_PASS=postgres  

- Con H2 en memoria (perfil `dev`):

DB_KIND=H2  

- **H2 Console**:  
  - URL: http://localhost:8080/h2-console  
  - JDBC URL: jdbc:h2:mem:pedidos  
  - Usuario: sa (sin contraseña)  

- Reinicio automático con devtools y debug remoto opcional:

mvn -Dspring-boot.run.profiles=dev -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005" spring-boot:run  

### Producción (`prod`)

- Configuración mediante `application-prod.yml` o variables de entorno estándar de Spring Boot:

export SPRING_DATASOURCE_URL=jdbc:postgresql://db-host:5432/pedidos  
export SPRING_DATASOURCE_USERNAME=prod_user  
export SPRING_DATASOURCE_PASSWORD=prod_secret  

java -Dspring.profiles.active=prod -jar target/pedidos-0.0.1-SNAPSHOT.jar

---

## Docker Compose para Postgres y pgAdmin

Este proyecto incluye un archivo `docker-compose.yml` para levantar Postgres y pgAdmin localmente.

                 ┌────────────────────────────┐
                 │        Docker Network      │
                 │ (red interna creada por Compose)
                 ├────────────────────────────┤
                 │                            │
                 │   ┌──────────────────┐     │
                 │   │  Service:        │     │
                 │   │  postgres        │◄───────────┐
                 │   │  Container:      │     │      │
                 │   │  pedidos-postgres│     │      │
                 │   │  Hostname:       │     │      │
                 │   │  postgres        │     │      │
                 │   └──────────────────┘     │      │
                 │                            │      │ 
                 │   ┌──────────────────┐     │      │
                 │   │  Service:        │     │      │
                 │   │  pgadmin         │────────────┘ 
                 │   │  Container:      │     │         
                 │   │  pedidos-pgadmin │     │         
                 │   │  Hostname:       │     │         
                 │   │  pgadmin         │     │         
                 │   └──────────────────┘     │         
                 │                            │
                 └────────────────────────────┘


### Levantar contenedores

docker-compose up -d  

### Ver logs de Postgres

docker-compose logs -f postgres  

### Detener y limpiar contenedores y volúmenes

docker-compose down -v  

### Acceso a pgAdmin

- URL: http://localhost:8081  
- Usuario: admin@admin.com  
- Contraseña: admin  

#### Configurar servidor Postgres en pgAdmin

- Hostname: postgres  
- Puerto: 5432  
- Usuario: postgres  
- Contraseña: postgres  

Después de levantar Postgres con Docker Compose, puedes ejecutar la aplicación en modo desarrollo (`dev`) usando `.env` o las variables por defecto.

## Notas adicionales

- `spring-boot-devtools` está incluido en `pom.xml` en scope `runtime` y permite reinicio automático al cambiar código fuente.  
- Para ver SQL generado por JPA, se configura `spring.jpa.show-sql=true` en `application-dev.yml`.  
- La configuración `dataSourceDev()` detecta automáticamente si `DB_KIND=POSTGRES` y crea un `PGSimpleDataSource`. Si no, utiliza H2 en memoria.  
- Se recomienda usar perfiles Spring (`dev` o `prod`) para separar entornos de desarrollo y producción.  
- Variables de entorno pueden usarse en lugar de `.env` si se prefiere.