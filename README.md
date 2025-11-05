# SpringBoot-Servicio-Pedidos

Estructura inicial del proyecto siguiendo Clean Architecture.

Paquetes principales:
- `domain` (entidades, value-objects, errores, eventos)
- `application` (use cases, puertos, dtos)
- `infrastructure` (adaptadores: persistence, rest, configuración)
- `shared` (utilidades, excepciones)

Nota de actualización
---------------------
Este proyecto ha sido actualizado para usar Java 21 (LTS) y Spring Boot 3.2.12.

- Java: ahora se requiere Java 21 (LTS). Asegúrate de tener JDK 21 instalado y activo en tu entorno.
- Spring Boot: el parent fue actualizado a `spring-boot-starter-parent` 3.2.12 para compatibilidad con Java 21 y Spring Framework 6 / Jakarta.

Consejos rápidos tras la migración:

- Recomendación: usa SDKMAN o la instalación de tu distribución para gestionar JDKs y dejar Java 21 como predeterminado durante el desarrollo.
- Revisa integraciones externas o librerías que dependan de `javax.*`; con Spring Boot 3 / Spring Framework 6 es posible que debas migrar a paquetes `jakarta.*` si no se ha hecho ya.

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

Usa el perfil `dev` para ejecutar la aplicación con H2 en memoria y reinicio automático (devtools).

```bash
mvn -Dspring-boot.run.profiles=dev spring-boot:run
```

```bash
# construir primero
mvn clean package -DskipTests

java -Dspring.profiles.active=dev -jar target/pedidos-0.0.1-SNAPSHOT.jar
```

- H2 console (por defecto cuando el perfil `dev` está activo):

	- URL: http://localhost:8080/h2-console
	- JDBC URL: jdbc:h2:mem:pedidos
	- Usuario: sa  (sin contraseña por defecto)

- Ejecutar con reinicio automático y habilitar debug remoto (opcional):

```bash
mvn -Dspring-boot.run.profiles=dev \
        -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005" \
        spring-boot:run
```

Notas rápidas:

- `spring-boot-devtools` está incluido en `pom.xml` en scope `runtime` y permite reinicio automático cuando cambias código fuente.
- Si quieres persistencia H2 en disco durante el desarrollo cambia la URL en `application-dev.yml` (p.ej. `jdbc:h2:file:./data/pedidos`).
- Para ver SQL en consola ya está configurado `spring.jpa.show-sql=true` en `application-dev.yml`.

## Modo desarrollo con Postgres (usar `.env`)

Si prefieres probar contra Postgres en local durante el desarrollo puedes crear un archivo `.env` en la raíz del proyecto o exportar las variables de entorno. El proyecto usa `java-dotenv` en dev y leerá `.env` automáticamente cuando exista.

Ejemplo mínimo de `.env` (archivo en la raíz del proyecto):

```bash
# .env
DB_KIND=POSTGRES
DB_URL=jdbc:postgresql://localhost:5432/pedidos
DB_USER=postgres
DB_PASS=secret
```

Comandos para ejecutar usando ese `.env` (no hace falta pasar credenciales por la línea de comandos):

```bash
# Construir
mvn clean package -DskipTests

# Ejecutar en dev (la configuración dev lee .env si existe)
mvn -Dspring-boot.run.profiles=dev spring-boot:run

# O ejecutar el JAR (asegúrate de tener .env presente en el directorio de trabajo)
java -Dspring.profiles.active=dev -jar target/pedidos-0.0.1-SNAPSHOT.jar
```

También puedes exportar variables si no quieres usar `.env`:

```bash
export DB_KIND=POSTGRES
export DB_URL=jdbc:postgresql://localhost:5432/pedidos
export DB_USER=postgres
export DB_PASS=secret
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

Consejos de seguridad y despliegue:

- No pongas contraseñas en control de versiones; usa gestores de secretos del proveedor (Azure Key Vault, AWS Secrets Manager, HashiCorp Vault) o variables de entorno proporcionadas por el entorno de ejecución.
- Ajusta el pool Hikari en `application-prod.yml` si necesitas mayor rendimiento.

## Archivos útiles

- `.env.example` — ejemplo de `.env` para desarrollo con Postgres (ya incluido en la raíz del proyecto).
- `src/main/resources/application-prod.yml` — ejemplo de propiedades para producción (Postgres).

## Probar Postgres local con Docker Compose

Si no tienes Postgres instalado localmente, puedes levantar uno rápido con Docker Compose que use los mismos valores de conexión que el proyecto por defecto.

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

### pgAdmin (opcional)

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

Nota: el `dataSourceDev()` detecta `DB_KIND=POSTGRES` si quieres usar `.env` en lugar de depender de los valores por defecto; de lo contrario, el Postgres levantado por Docker escuchará en `localhost:5432` y `dataSourceDev()` usará esa URL si configuras `DB_URL` en `.env`.

Si quieres, puedo:

- A: Añadir un `.env.example` (si quieres otro formato o valores diferentes).
- B: Añadir una migración básica con Flyway para crear las tablas iniciales en Postgres.
- C: Incluir un pequeño script de `docker-compose.yml` para levantar Postgres localmente y probar.

Indícame cuál de esas opciones prefieres y la implemento.
