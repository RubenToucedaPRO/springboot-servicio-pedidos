# 🧭 Instrucciones para Copilot: Cumplimientos de Clean Architecture en API REST con Spring Boot

Estas directrices deben aplicarse desde la inicialización del proyecto hasta su evolución continua. Copilot debe guiar las sugerencias, generación de código y organización de archivos respetando **Clean Architecture**, **convenciones Java**, y **principios de independencia del dominio**.

---

## 🧩 I. Principios Fundamentales

1. **Regla de Dependencias:** Toda dependencia debe apuntar hacia el **núcleo del dominio**. Ningún componente del dominio debe depender de infraestructura, frameworks ni controladores.
2. **Separación del QUÉ y el CÓMO:** El dominio define el *qué hace* la aplicación, y las capas externas implementan *cómo lo hace*.
3. **Modularidad:** Crear módulos independientes por dominio funcional (`products`, `recipes`, `users`), sin acoplamiento cruzado.
4. **Responsabilidad Única:** Cada clase o paquete debe tener un único propósito y motivo de cambio.

---

## 🏗 II. Estructura del Proyecto (Paquetes)

Copilot debe proponer y mantener la siguiente estructura (ejemplo con package base `com.pedidos`):

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

**Regla:** Ningún paquete interno importa nada de uno externo. Mantén el dominio y la capa de aplicación libres de dependencias de Spring/JPA.

---

## 🧠 III. Dominio (Reglas de Negocio)

* El dominio **no conoce Spring, ni JPA, ni HTTP**.
* Define:

  * **Entidades:** con identidad (`id`) y comportamiento.
  * **Value Objects:** inmutables y con validaciones internas.
  * **Servicios de Dominio:** para lógica que involucre varias entidades.
* Ejemplo:

  ```java
  public record Price(BigDecimal value) {
      public Price {
          if (value.compareTo(BigDecimal.ZERO) < 0) {
              throw new IllegalArgumentException("Price cannot be negative");
          }
      }
  }
  ```

---

## ⚙️ IV. Capa de Aplicación (Casos de Uso)

* Contiene **Casos de Uso** que orquestan la lógica:

  * Validan datos de entrada.
  * Invocan entidades o servicios de dominio.
  * Usan puertos para persistencia o comunicación externa.
* Define los **puertos** (interfaces) de entrada/salida.
* Los **DTOs** deben ser simples (`String`, `int`, etc.).
* Ejemplo:

  ```java
  public class CreateProductUseCase {
      private final SaveProductPort saveProductPort;

      public CreateProductUseCase(SaveProductPort saveProductPort) {
          this.saveProductPort = saveProductPort;
      }

      public void execute(CreateProductCommand command) {
          Product product = new Product(command.name(), new Price(command.price()));
          saveProductPort.save(product);
      }
  }
  ```

---

## 🌐 V. Infraestructura y Adaptadores

* Implementa los **puertos** definidos en la capa de aplicación.
* Se encarga de interacciones tecnológicas:

  * Repositorios (`JpaRepository`, JDBC, Redis…)
  * Controladores REST
  * Configuración de dependencias
* Ejemplo:

  ```java
  @Repository
  public class JpaProductRepository implements SaveProductPort {
      private final SpringDataProductRepository repository;

      public JpaProductRepository(SpringDataProductRepository repository) {
          this.repository = repository;
      }

      @Override
      public void save(Product product) {
          repository.save(ProductEntity.fromDomain(product));
      }
  }
  ```

---

## 🧪 VI. Testing

1. **Dominio:** tests unitarios puros, sin mocks ni frameworks.
2. **Aplicación:** tests con fakes o repositorios en memoria.
3. **Infraestructura:** tests de contrato e integración.
4. **Estrategia:** `domain` → rápido y puro; `infrastructure` → realista.

---

## 🚫 VII. Anti-patrones a Evitar

* ❌ Importar Spring o JPA en el dominio o aplicación.
* ❌ Controladores con lógica de negocio.
* ❌ Casos de uso anémicos sin orquestación.
* ❌ Exponer entidades del dominio como respuesta HTTP.
* ❌ Mutar estados en entidades/VOs.
* ❌ Usar singletons globales no controlados.

---

## 🧭 VIII. Convenciones Java

* **Clases:** `PascalCase`
* **Variables y métodos:** `camelCase`
* **Constantes:** `UPPER_SNAKE_CASE`
* **Interfaces de puertos:** `XxxPort`
* **Casos de uso:** `Verbo + Entidad` (Ej: `CreateOrder`, `DeleteProduct`)
* **Tests:** `ClassNameTest` en `src/test/java`

---

## ✅ IX. Reglas para Copilot

* Sugerir siempre código alineado con las capas definidas.
* No importar dependencias externas dentro del dominio.
* Respetar la inversión de dependencias.
* Sugerir constructores con inyección de dependencias explícita.
* Priorizar tests unitarios y fakes sobre mocks complejos.
* Recomendar modularización por dominio, no por tipo técnico.

---

**Objetivo Final:**
El proyecto debe mantener independencia del dominio, claridad de responsabilidades, alta testabilidad y mínima fricción con el framework.
