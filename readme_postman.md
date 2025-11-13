## README — Usar la colección Postman "Pedidos"

Este documento explica cómo usar la colección Postman adjunta (`Pedidos.postman_collection.json`) con la aplicación `SpringBoot-Servicio-Pedidos` que tienes en este repositorio.

Resumen rápido

- Importa la colección de Postman adjunta.
- Crea un entorno (Environment) con las variables `base_url` y `order`.
- Ajusta `base_url` a tu servidor local (por ejemplo `http://localhost:8080/api/orders`).
- Ejecuta las peticiones: crear pedido.
- Actualiza la variable `order` con el ID del pedido creado.
- Ejecuta las peticiones: añadir item, obtener pedido, borrar pedido.

Requisitos previos

- Java 21 y Maven instalados.
- Postman (o Newman) para importar y ejecutar la colección.
- La aplicación corriendo localmente: desde la raíz del proyecto ejecuta

```bash
mvn -Dspring-boot.run.profiles=dev spring-boot:run
```

Por defecto la aplicación se sirve en el puerto 8080. El controlador REST expone los endpoints bajo el prefijo `/api/orders`.

Variables de entorno (Environment)

En Postman crea un nuevo Environment llamado `pPdidos` con estas variables:

- `base_url` = `http://localhost:8080/api/orders`
- `order` = `` (vacío — se rellenará cuando crees un pedido)

Importar la colección

1. En Postman: File → Import → elegir `Pedidos.postman_collection.json` desde el proyecto.
2. Selecciona la colección importada (debería llamarse **Pedidos**) y el Environment `Pedidos`.

Mapeo de las peticiones de la colección a los endpoints reales

La colección que adjuntaste contiene plantillas genéricas. A continuación tienes las rutas reales que expone la aplicación y cómo adaptarlas:

- Crear pedido (POST)
  - URL en Postman: `{{base_url}}` (método POST)
  - Body (raw JSON):

```json
{
  "items": [
    {
      "productId": "P-1",
      "quantity": 2,
      "unitPrice": 10.50,
      "currency": "EUR"
    }
  ]
}
```

  - Respuesta esperada: 201 Created con body
    `{ "orderId": "<uuid>" }`.
  - Acción útil: añade un test de Postman que guarde `orderId` en la variable de entorno `order` para usarlo en las siguientes peticiones:

```javascript
// Test script para almacenar el orderId
pm.test("POST created", function () {
  pm.response.to.have.status(201);
  const json = pm.response.json();
  if (json && json.orderId) {
    pm.environment.set("order", json.orderId);
  }
});
```

- Añadir item a un pedido (POST)
  - URL en Postman: `{{base_url}}/{{order}}/items` (método POST)
  - Body (raw JSON) — la colección ya tiene un ejemplo; aquí uno mínimo (unitPrice opcional si confías en pricing):

```json
{
  "productId": "P-5",
  "quantity": 10,
  "unitPrice": 5.00,
  "currency": "USD"
}
```

  - Respuesta esperada: 200 OK con `{ "orderId": "<uuid>" }` o similar.

- Obtener pedido (GET)
  - URL en Postman: `{{base_url}}/{{order}}` (método GET)
  - Respuesta esperada: 200 OK con un JSON con la representación del pedido:

```json
{
  "orderId": "<uuid>",
  "items": [ { "productId": "P-1", "quantity": 2, "unitPrice": 10.50, "currency": "EUR" } ],
  "totals": { "EUR": 21.00 }
}
```

- Borrar pedido (DELETE)
  - URL en Postman: `{{base_url}}/{{order}}` (método DELETE)
  - Respuesta esperada: 200 OK y mensaje simple (ej. "Deleted successfully: <orderId>").

Errores y códigos HTTP

- 400 Bad Request — `validation_error` (por ejemplo datos faltantes o inválidos: cantidad <= 0, moneda no soportada, id inválido).
- 404 Not Found — `not_found` (pedido no encontrado).
- 409 Conflict — `conflict` (si aplica lógica de conflicto en el repositorio).
- 500 Internal Server Error — `infra_error` (problemas internos de infraestructura).

Consejos prácticos

- Antes de ejecutar la colección, asegúrate que la variable `base_url` está bien (sin sufijos duplicados). Por ejemplo correcto: `http://localhost:8080/api/orders`.
- Usa el test script mostrado arriba en la petición de creación para propagar `order` automáticamente.
- Si tu Collección usa rutas distintas (p. ej. `{{base_url}}/post`), edítalas para que apunten a los endpoints reales mostrados aquí.
- Si quieres ejecutar la colección desde la terminal usa Newman:

```bash
npx newman run Pedidos.postman_collection.json -e local-pedidos.postman_environment.json
```

(Para Newman necesitarás exportar también el Environment desde Postman como archivo `local-pedidos.postman_environment.json`.)

Notas finales

- La colección adjunta es una plantilla; adáptala cambiando `base_url` y las rutas para que apunten a `/api/orders` y usar las variables `order` donde corresponda.
- Si quieres que yo actualice la colección para que incluya los endpoints exactos (y el test script para guardar `order`), puedo modificar el archivo `Pedidos.postman_collection.json` y añadir un environment export listo para importar.

---

Archivo relacionado en este repo:

- `Pedidos.postman_collection.json` (colección Postman adjunta)
