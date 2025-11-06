# SpringBoot-Servicio-Pedidos

Estructura del proyecto siguiendo Clean Architecture.

Paquetes principales:
- `domain` (entidades, value-objects, errores, eventos)
- `application` (use cases, puertos, dtos)
- `infrastructure` (adaptadores: persistence, rest, configuración)
- `shared` (utilidades, excepciones)

---------------------
Este proyecto usa Java 21 (LTS) y Spring Boot 3.2.12.

- Java: se requiere JDK 21.
- Spring Boot: parent `spring-boot-starter-parent` 3.2.12 (compatible con Spring Framework 6 / Jakarta).

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


## Comandos útiles

Requisitos: Java 21 (LTS) y Maven instalados.

- Limpiar el proyecto (elimina `target/`):

```bash
mvn clean
```

- Compilar y empaquetar (genera el JAR en `target/`):

```bash
mvn clean package
```

- Empaquetar sin ejecutar tests (útil para iteraciones rápidas):

```bash
mvn clean package -DskipTests
```

- Ejecutar la aplicación desde Maven (arranca Spring Boot):

```bash
mvn spring-boot:run
```

- Ejecutar el JAR generado:

```bash
java -jar target/pedidos-0.0.1-SNAPSHOT.jar
```

- Ejecutar tests unitarios:

```bash
mvn test
```

- Ejecutar un test concreto (por clase):

```bash
mvn -Dtest=NombreClaseTest test
```

- Ejecutar con un perfil Spring activo (ej: `dev`):

```bash
mvn -Dspring-boot.run.profiles=dev spring-boot:run
# o al ejecutar el JAR:
java -jar -Dspring.profiles.active=dev target/pedidos-0.0.1-SNAPSHOT.jar
```

## Modo desarrollo (dev)
Puedes probar contra Postgres en local durante el desarrollo creando un archivo `.env` en la raíz del proyecto. El proyecto usa `java-dotenv` en dev y leerá `.env` automáticamente cuando exista.

Ejemplo mínimo de `.env` (archivo en la raíz del proyecto):

```bash
# .env
DB_KIND=POSTGRES
DB_URL=jdbc:postgresql://localhost:5432/pedidos
DB_USER=postgres
DB_PASS=postgres
```

Usa el perfil `dev` para ejecutar la aplicación sin .env con H2 en memoria y reinicio automático (devtools). En caso de usar  el `.env` con H2 modifica `DB_KIND`:

```bash
# .env
DB_KIND=H2
DB_URL=jdbc:postgresql://localhost:5432/pedidos
DB_USER=postgres
DB_PASS=postgres
```
	- H2 console (por defecto cuando el perfil `dev` está activo):

      - URL: http://localhost:8080/h2-console
      - JDBC URL: jdbc:h2:mem:pedidos
      - Usuario: sa  (sin contraseña por defecto)

Comandos para ejecutar en modo dev::
```bash
mvn -Dspring-boot.run.profiles=dev spring-boot:run
```

O construir y ejecutar el JAR en modo dev:
```bash
# construir primero
mvn clean package -DskipTests

java -Dspring.profiles.active=dev -jar target/pedidos-0.0.1-SNAPSHOT.jar
```

- Ejecutar con reinicio automático y habilitar debug remoto (opcional):

```bash
mvn -Dspring-boot.run.profiles=dev \
        -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005" \
        spring-boot:run
```

Notas rápidas:

- `spring-boot-devtools` está incluido en `pom.xml` en scope `runtime` y permite reinicio automático cuando cambias código fuente.
- Para ver SQL en consola ya está configurado `spring.jpa.show-sql=true` en `application-dev.yml`.


## O ejecutar el JAR (asegúrate de tener .env presente en el directorio de trabajo)
java -Dspring.profiles.active=dev -jar target/pedidos-0.0.1-SNAPSHOT.jar
```

También puedes exportar variables si no quieres usar `.env`:

```bash
export DB_KIND=POSTGRES
export DB_URL=jdbc:postgresql://localhost:5432/pedidos
export DB_USER=postgres
export DB_PASS=postgres
mvn -Dspring-boot.run.profiles=dev spring-boot:run
```

> Nota: la configuración `dataSourceDev()` intenta detectar si `DB_KIND=POSTGRES` y, en ese caso, crea un `PGSimpleDataSource`. Si no se detecta, usa H2 en memoria.

## Producción (perfil `prod`)

En producción se recomienda usar el perfil `prod` y proporcionar las propiedades estándar de Spring Boot (`spring.datasource.*`). El proyecto incluye `src/main/resources/application-prod.yml` con un ejemplo de configuración para Postgres.

Ejemplo de ejecución con perfil `prod`:

```bash
# Usando Maven
mvn -Dspring-boot.run.profiles=prod spring-boot:run

# O al ejecutar el JAR
java -Dspring.profiles.active=prod -jar target/pedidos-0.0.1-SNAPSHOT.jar
```

Opciones para pasar la configuración de la base de datos en producción:

- Usar `application-prod.yml` (ya añadido como ejemplo en `src/main/resources`).
- O establecer las variables de entorno estándar de Spring Boot antes de arrancar:

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://db-host:5432/pedidos
export SPRING_DATASOURCE_USERNAME=prod_user
export SPRING_DATASOURCE_PASSWORD=prod_secret
java -Dspring.profiles.active=prod -jar target/pedidos-0.0.1-SNAPSHOT.jar
```


## Probar Postgres local con Docker Compose

Puedes levantar uno rápido con Docker Compose que use los mismos valores de conexión que el proyecto por defecto.

Archivo `docker-compose.yml` (incluido en la raíz del proyecto):

```yaml
version: '3.8'
services:
	postgres:
		image: postgres:15-alpine
		environment:
			POSTGRES_USER: postgres
			POSTGRES_PASSWORD: postgres
			POSTGRES_DB: pedidos
		ports:
			- "5432:5432"
		volumes:
			- pedidos-db-data:/var/lib/postgresql/data
```

Comandos rápidos:

```bash
# Levantar Postgres en segundo plano
docker-compose up -d

# Ver logs (opcional)
docker-compose logs -f postgres

# Parar y eliminar contenedor y volumen (limpieza)
docker-compose down -v
```

### pgAdmin

Si prefieres usar una interfaz gráfica para explorar la base de datos, este repositorio incluye un servicio `pgadmin` en `docker-compose.yml`.

- Accede a: http://localhost:8081
- Credenciales por defecto: `pgadmin@local` / `pgadmin`
- Dentro de pgAdmin añade un servidor con Host: `postgres`, Port: `5432`, Username: `postgres`, Password: `postgres`.

Nota: cuando configures el servidor dentro de pgAdmin (que corre en otro contenedor), utiliza el hostname `postgres` (la red de Docker los resuelve). Si conectas desde tu máquina directamente usa `localhost:5432`.

Después de levantar Postgres con `docker-compose up -d` puedes ejecutar la app en modo dev (usa `.env` o las variables por defecto):

```bash
# Con Maven
mvn -Dspring-boot.run.profiles=dev spring-boot:run

# O construir y ejecutar el JAR
mvn clean package -DskipTests
java -Dspring.profiles.active=dev -jar target/pedidos-0.0.1-SNAPSHOT.jar
```