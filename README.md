# 🟢 Caprichómetro

> Tus caprichos, tus dudas y tus planes en 5 minutos.

Caprichómetro es una aplicación Android nativa diseñada para gestionar decisiones, caprichos y planes de forma rápida e intuitiva, construida bajo estrictos estándares de código limpio, separación de responsabilidades y **Arquitectura Hexagonal**.

---

## 🏗 Arquitectura y Estructura del Proyecto

El proyecto aísla la lógica de negocio de los frameworks externos dividiéndose en tres capas principales:

- **Dominio (`domain`):** Modelos puros de negocio (como `Caprice`) y puertos (interfaces de repositorio como `CapriceRepository`). Sin dependencias de Android.
- **Aplicación (`application`):** Casos de uso que orquestan las operaciones (`SaveCapriceUseCase`, `GetCapricesUseCase`).
- **Infraestructura (`infrastructure`):** Adaptadores tecnológicos y UI.
    - **Data (`infrastructure/data`):** Implementaciones reales contra Firebase Firestore (`FirestoreCapriceRepository`).
    - **UI (`infrastructure/ui`):** Pantallas construidas 100% con Jetpack Compose (`CapriceScreen`, `ActivePollScreen`, `ResultScreen`), ViewModels y navegación basada en estados locales.

---

## 🛠 Trazabilidad de Cambios: Clases Creadas y Modificadas

Para implementar con éxito el flujo completo de creación, persistencia filtrada por usuario, cuenta atrás en tiempo real y transición a la pantalla de resultados, se han estructurado y modificado los siguientes componentes:

### 1. Capa de Dominio (`domain`)
* **`Caprice.kt` (Modificada):** Se añadió el campo `userId: String` para asociar cada capricho de forma unívoca al usuario autenticado.
* **`CapriceRepository.kt` (Modificada):** Se añadió el contrato de lectura asíncrona filtrada: `suspend fun getCaprices(userId: String, limit: Int): Result<List<Caprice>>`.

### 2. Capa de Aplicación (`application`)
* **`GetCapricesUseCase.kt` (Nueva):** Caso de uso encargado de invocar el repositorio para recuperar los caprichos del usuario activo.

### 3. Capa de Infraestructura - Datos (`infrastructure/data`)
* **`FirestoreCapriceRepository.kt` (Modificada):** Implementación del método de lectura en Firestore. Aplica el filtrado por `whereEqualTo("userId", userId)`, ordena cronológicamente de forma descendente por `timestamp` y limita los registros. *(Requiere un Índice Compuesto en Firestore para `userId (Ascending)` y `timestamp (Descending)`)*.

### 4. Capa de Infraestructura - UI y Presentación (`infrastructure/ui`)
* **`CapriceViewModel.kt` (Modificada):** Inyección del caso de uso `GetCapricesUseCase`, integración con `FirebaseAuth` para obtener el identificador del usuario activo, y actualización del estado mediante `CapriceState` para almacenar una lista de caprichos recientes (`latestCaprices`).
* **`CapriceScreen.kt` (Modificada):** Interfaz de creación de dudas (tipo Compra/Plan) con validaciones de campos de texto y llamada automática al ViewModel para guardar y refrescar el último registro antes de navegar.
* **`ActivePollScreen.kt` (Modificada):** Pantalla del temporizador cuenta atrás de 5 minutos. Utiliza un `LaunchedEffect` combinado con `delay(1000L)` para calcular en tiempo real el tiempo restante a partir del `timestamp` de Firestore. Al finalizar, invoca el callback `onTimerFinished()`.
* **`ResultScreen.kt` (Nueva):** Pantalla de veredicto final que muestra el cierre de la votación y barras de progreso porcentuales estéticas basadas en las respuestas del modelo.
* **`MainActivity.kt` (Modificada):** Enrutador declarativo central basado en una máquina de estados local (`currentScreen` controlada mediante un bloque `when`), encargada de inyectar manualmente las dependencias e interconectar las pantallas mediante funciones lambda (*callbacks*).

---

## 🤖 Guía Técnica y Reglas para el Agente (Coding Rules)

> **Instrucción obligatoria para cualquier IA o asistente de desarrollo que colabore en este repositorio:** Si vas a modificar, ampliar o depurar código en este proyecto, estás estrictamente obligado a cumplir las siguientes directrices arquitectónicas:

1. **Respeto Absoluto a la Arquitectura Hexagonal:**
    * **Nunca** introduzcas llamadas directas a Firebase, SDKs de Android o frameworks externos dentro de las capas de `domain` o `application`. La lógica de negocio debe ser completamente agnóstica.
2. **Flujo Unidireccional de Datos (UDF):**
    * Las pantallas de Jetpack Compose son componentes visuales desacoplados. No deben mutar estados globales directamente; cualquier operación de lectura, escritura o lógica debe delegarse en los `ViewModels` a través de los Casos de Uso correspondientes.
3. **Gestión Declarativa de Navegación:**
    * La navegación entre pantallas se resuelve mediante enrutamiento por estados en la actividad principal y paso de funciones lambda (`callbacks`), evitando acoplar las pantallas a dependencias de navegación externas innecesarias en flujos de esta escala.
4. **Clean Code y Convenciones de Nomenclatura:**
    * Mantén la nomenclatura de variables, funciones, clases y archivos en **inglés**, siguiendo estrictamente los estándares de desarrollo en Kotlin para Android.
    * Evita comentarios redundantes. El código fuente debe ser completamente autoexplicativo.
5. **Tipado y Null-Safety:**
    * Gestiona siempre los estados de carga, nulos o asíncronos utilizando los operadores seguros de Kotlin (`?.`, `?:`) al consumir datos dinámicos procedentes de servicios externos como Firebase Firestore.