# SpringBoot-Servicio-Pedidos

Estructura inicial del proyecto siguiendo Clean Architecture.

Paquetes principales:
- `domain` (modelo, servicios, eventos)
- `application` (use cases, puertos)
- `infrastructure` (adaptadores: persistence, rest, configuración)
- `shared` (utilidades, excepciones)

Rellenar `pom.xml` con dependencias de Spring Boot y versiones deseadas.

## Comandos útiles

Requisitos: Java (11+ recomendado) y Maven instalados.

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

## Siguientes pasos recomendados

- Completar `pom.xml` con la versión de Spring Boot y dependencias (spring-boot-starter-web, spring-boot-starter-data-jpa, etc.).
- Añadir entidades y Value Objects en `src/main/java/com/example/pedidos/domain`.
- Implementar casos de uso en `application/usecase` y los puertos en `application/port`.
- Implementar adaptadores en `infrastructure` (JPA repositories, controladores REST, configuración).

Si quieres, puedo:

- Añadir un `pom.xml` base con dependencias recomendadas de Spring Boot.
- Reemplazar `.gitkeep` por `README.md` explicativos en carpetas seleccionadas.

Indica qué prefieres y continúo.

## Modo desarrollo (dev)

Usa el perfil `dev` para ejecutar la aplicación con H2 en memoria y reinicio automático (devtools).

- Ejecutar con Maven y perfil `dev`:

```bash
mvn -Dspring-boot.run.profiles=dev spring-boot:run
```

- Ejecutar el JAR con el perfil `dev`:

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

