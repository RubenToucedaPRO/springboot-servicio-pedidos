# 🐳 Guía: Docker + PostgreSQL para desarrollo (Spring Boot)

Esta guía te acompaña paso a paso para levantar una base de datos PostgreSQL con Docker y usarla desde tu aplicación Spring Boot en modo *dev*.

Contenido rápido
- Requisitos
- Instalar Docker & Docker Compose
- Crear y usar `docker-compose.yml`
- Arrancar el contenedor y comprobar estado
- Conectar desde la aplicación (usar `.env`)
- Comandos útiles y resolución de problemas

---

## Requisitos

- Sistema operativo con Docker disponible (Linux, macOS, Windows).
- Docker instalado (y, preferiblemente, el plugin `docker compose`).
- Acceso a terminal/CLI.

Si usas Ubuntu, en muchos casos instalar `docker.io` y el plugin de compose es suficiente (ver abajo).

---

## 1. Instalar Docker (Ubuntu)

Ejecuta en una terminal:

```bash
sudo apt update
sudo apt install -y docker.io
```

Comprueba la versión:

```bash
docker --version
```

Instala el plugin de Compose (recomendado):

```bash
sudo apt install -y docker-compose-plugin
```

Verifica:

```bash
docker compose version
```

> Nota: el comando moderno es `docker compose` (sin guion). Si prefieres, también puedes instalar el binario `docker-compose`, pero el plugin es la vía recomendada hoy en día.

---

## 2. Crear `docker-compose.yml` (en la raíz del proyecto)

Crea un archivo `docker-compose.yml` con este contenido (ya incluido en el repositorio):

```yaml
services:
  postgres:
    image: postgres:15-alpine
    container_name: pedidos-postgres
    restart: unless-stopped
    environment:
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
      POSTGRES_DB: pedidos
    ports:
      - "5432:5432"
    volumes:
      - pedidos-db-data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U $${POSTGRES_USER} -d $${POSTGRES_DB}"]
      interval: 10s
      timeout: 5s
      retries: 5

volumes:
  pedidos-db-data:
```

Explicación rápida:
- Usa la imagen oficial `postgres:15-alpine`.
- Crea la BD `pedidos` con usuario `postgres`.
- Expone el puerto `5432` en `localhost`.
- Persiste datos en un volumen Docker para no perder datos al reiniciar.

---

## 3. Levantar Postgres con Docker Compose

Desde la raíz del proyecto ejecuta:

```bash
docker compose up -d
```

Comprobaciones:

```bash
docker compose ps
docker compose logs --tail 100 postgres
```

La salida de `docker compose ps` debería mostrar `Up (healthy)` cuando esté listo.

Si recibes `permission denied` al conectar con el socket de Docker (`/var/run/docker.sock`), añade tu usuario al grupo `docker`:

```bash
sudo usermod -aG docker $USER
# luego cierra sesión y vuelve a entrar, o ejecuta `newgrp docker`
```

---

## 4. Conectar la aplicación (usar `.env`)

La configuración de `InfrastructureConfiguration` en modo `dev` puede leer un archivo `.env` si existe. Crea un `.env` en la raíz del proyecto con estos valores:

```env
DB_KIND=POSTGRES
DB_URL=jdbc:postgresql://localhost:5432/pedidos
DB_USER=postgres
DB_PASS=postgres
```

Con eso, `dataSourceDev()` detectará `DB_KIND=POSTGRES` y creará una conexión a Postgres usando esas variables.

---

## 5. Ejecutar la aplicación en modo dev

Opción A — Ejecutar con Maven (recomendado durante desarrollo):

```bash
mvn -Dspring-boot.run.profiles=dev spring-boot:run
```

Opción B — Construir JAR y ejecutar:

```bash
mvn clean package -DskipTests
java -Dspring.profiles.active=dev -jar target/pedidos-0.0.1-SNAPSHOT.jar
```

Al arrancar, el repositorio Postgres (`PostgresOrderRepository`) intentará crear las tablas necesarias si no existen, así que normalmente no necesitas ejecutar migraciones manuales para pruebas locales (aunque para producción es recomendable usar Flyway/Liquibase).

---

## 6. Entrar en el contenedor y usar `psql`

Para abrir una consola SQL dentro del contenedor:

```bash
docker exec -it pedidos-postgres psql -U postgres -d pedidos
```

Comandos útiles dentro de `psql`:

- `\l` — listar bases de datos
- `\dt` — listar tablas
- `\d table_name` — ver esquema de una tabla
- `SELECT * FROM orders;` — ejecutar consulta
- `\q` — salir

---

## 7. Problemas frecuentes y soluciones

- Error: `permission denied` al conectar con Docker
  - Solución: añade tu usuario al grupo `docker` (ver arriba) o ejecuta `sudo docker compose up -d`.

- Error: `docker compose` no encuentra la imagen (o falta `docker compose`)
  - Solución: instala el plugin `docker-compose-plugin` o usa el binario `docker-compose`.

- La aplicación no conecta a la BD (connection refused)
  - Asegúrate de que el contenedor está `Up (healthy)` y que la URL en `.env` coincide con la URL del servicio. Revisa `docker compose logs postgres`.

---

## 8. pgAdmin (UI web para Postgres)

Si prefieres una interfaz gráfica para explorar la base de datos, este repositorio incluye un servicio `pgadmin` dentro del `docker-compose.yml`.

1. Accede a la web de pgAdmin en tu navegador:

```text
http://localhost:8081
```

2. Credenciales por defecto (configuradas en `docker-compose.yml`):

- Email: `pgadmin@local`
- Password: `pgadmin`

3. Añadir el servidor Postgres dentro de pgAdmin:

- Hostname/address: `postgres`
- Port: `5432`
- Maintenance database: `postgres` (o `pedidos` si prefieres)
- Username: `postgres`
- Password: `postgres`

Nota: al usar el `docker-compose` incluido, `pgadmin` y `postgres` comparten la misma red de Docker; por eso el hostname del servidor es `postgres` (no `localhost`) cuando se configura desde la interfaz de pgAdmin que corre en otro contenedor. Si accedes con un cliente desde tu máquina local (fuera de Docker), la conexión seguirá siendo `localhost:5432`.

Si quieres cambiar las credenciales por defecto, edita las variables `PGADMIN_DEFAULT_EMAIL` y `PGADMIN_DEFAULT_PASSWORD` en `docker-compose.yml`.
