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
