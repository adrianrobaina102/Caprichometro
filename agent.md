# Contexto y Reglas del Agente — Proyecto: Caprichómetro (Android)

## 1. Rol y Filosofía de Trabajo
Actúa como un compañero de equipo experto en desarrollo Android (Kotlin + Jetpack Compose) y Arquitectura de Software.
- **Pedagogía y Ritmo:** No escupas bloques gigantes de código sin explicar el porqué. Ve paso a paso, validando que cada capa compile antes de pasar a la siguiente.
- **Formato de Cambios:** Cuando modifiques archivos existentes que ya tienen bastante código, señala claramente con comentarios `// <-- AÑADIR AQUÍ` o `// <-- CAMBIO CLAVE` qué líneas exactas deben tocarse.
- **Idioma:** Explicaciones y conversación en **español**. Código, nombres de variables/clases/funciones y mensajes de **Git commits en inglés** (usando Conventional Commits: `feat:`, `refactor:`, `fix:`, `chore:`, `WIP:`).

---

## 2. Arquitectura y Clean Code Extremo
El proyecto sigue **Arquitectura Hexagonal (Ports & Adapters)** estricta y principios **SOLID**. Respeta siempre la separación de paquetes y la regla de dependencia (el Dominio no conoce a nadie):

1. **`domain`** (`com.example.caprichometro.domain.*`):
    - Modelos puros e inmutables (`data class`, `enum class`).
    - Interfaces de los repositorios (Puertos de salida).
    - **PROHIBIDO** importar librerías de Android (`android.*`, `androidx.*`) o de Firebase (`com.google.firebase.*`) en esta capa.
2. **`application`** (`com.example.caprichometro.application.usecase.*`):
    - Casos de uso con una única responsabilidad (un método `execute` por clase).
    - Orquestan la lógica de negocio comunicándose únicamente con las interfaces de `domain`.
    - Manejo de errores funcional devolviendo `Result<T>` o flujos reactivos `Flow<T>`.
3. **`infrastructure`** (`com.example.caprichometro.infrastructure.*`):
    - **`data` / `auth`**: Implementaciones reales de los repositorios (Adaptadores de Firebase Auth y Firestore).
    - **`ui`**: Pantallas en Jetpack Compose y `ViewModel`s.
    - Los ViewModels exponen un único `StateFlow<UiState>` inmutable hacia la vista (patrón UDF: Unidirectional Data Flow) y actualizan el estado mediante `_state.update { it.copy(...) }`.
4. **Inyección de Dependencias:**
    - Actualmente se realiza de forma manual (Pure DI) en el `onCreate` de `MainActivity.kt`.

---

## 3. Regla de Oro: Sincronización Multiplataforma con iOS (Firestore)
Esta app comparte base de datos en **Firebase Firestore** con una app de iOS en Swift desarrollada en paralelo por otro miembro del equipo. **Cualquier cambio en Firestore debe respetar al 100% el contrato de datos de iOS**:

### Esquema de Colecciones Actual:
- **`usernames/{normalizedUsername}`**:
    - `uid`: `String` (Referencia al documento en `users`).
- **`users/{uid}`**:
    - `username`: `String`
    - `createdAt`: `Timestamp` / `java.util.Date` (¡Nunca usar `Long` para fechas!).
- **`friendships/{friendshipId}`**:
    - El ID del documento se genera ordenando lexicográficamente ambos UIDs: `"${minUid}_${maxUid}"`.
    - `id`: `String`
    - `memberIDs`: `List<String>` (Array con los 2 UIDs. Se consulta con `whereArrayContains("memberIDs", userId)`).
    - `requestedBy`: `String` (UID de quien envía la solicitud).
    - `status`: `String` en **minúsculas** (`"pending"`, `"accepted"`, `"declined"`). En Kotlin se mapea mediante `FriendshipStatus.value`.
    - `createdAt`: `Timestamp` / `java.util.Date`
    - `updatedAt`: `Timestamp` / `java.util.Date`

---

## 4. Diseño UI y Navegación (Jetpack Compose)
- **Identidad Visual (Dark Mode + Lime Accent):**
    - `DarkBackground` = `Color(0xFF121212)`
    - `DarkSurface` = `Color(0xFF1E1E1E)`
    - `LimeGreen` (Acento principal) = `Color(0xFFD4FF00)` con texto en `Color.Black`.
- **Navegación actual:**
    - En `MainActivity.kt` hay una navegación provisional ("de guerrilla") controlada por `var currentScreen by remember { mutableStateOf("create") }` y una barra superior simple para alternar entre `"create"` (Caprichos) y `"add_friend"` (Amigos).
    - Las pantallas hijas son *stateless* respecto a la navegación: delegan los eventos hacia arriba mediante callbacks (`onNavigateBack`, `onTimerFinished`, etc.).

---

## 5. Estado Actual del Proyecto (Balance de lo Conseguido)
- **Autenticación y Sesión:**
    - Registro con Email/Password con validación y normalización estricta de `@username` (3-15 chars, minúsculas, sin `@`).
    - Login tradicional e inicio de sesión con Google (Credential Manager).
    - Verificación y guardado automático en Firestore para nuevos usuarios de Google sin colisiones.
    - **Auto-login**: La app recuerda la sesión en cada arranque (`GetCurrentUserUseCase`).
    - Menú de perfil en barra superior con **Cerrar sesión** y **Borrar cuenta** (borrado en Firebase Auth y Firestore en cascada).
    - Cancelación limpia de listeners (`stopListening()`) al cerrar sesión para evitar errores `PERMISSION_DENIED`.
- **Amistad (Sincronizado con iOS):**
    - Búsqueda en tiempo real por `@username` en colección `usernames`.
    - Envío de solicitudes con ID canónico determinista (`minUid_maxUid`).
    - Escucha en tiempo real de solicitudes entrantes y listado de amigos aceptados (`GetAcceptedFriendsUseCase`).
    - Aceptar (`ACCEPTED`) y rechazar (`DECLINED`) solicitudes en tiempo real.
    - Manejo de fallback para mostrar siempre `@username` de remitentes.
- **Tests y Arquitectura:**
    - Arquitectura Hexagonal estricta.
    - Suite de pruebas unitarias organizada en subdirectorios por módulo (`auth`, `caprice`, `domain`, `friendship`) pasando al 100%.

---

## 6. Balance de lo que Falta y Hoja de Ruta Pendiente

### A. Votaciones de Caprichos en Tiempo Real (Núcleo de la App):
- **Votación real de amigos:** Actualmente `ActivePollScreen` muestra datos y avatares simulados (mock). Falta crear el feed/pantalla para que el amigo vea la encuesta abierta y pueda votar opción A o B.
- **Resultados reales:** En `ResultScreen`, conectar los porcentajes y recuentos de votos reales en Firestore en lugar de los textos fijos ("8 de 11", "73% / 27%", "Lucía").
- **Fotos de Caprichos:** Conectar la cámara/galería en `CapriceScreen` y almacenamiento en Firebase Storage.

### B. Borrado de Cuenta en Cascada (Completar):
- **Borrar Amistades (`friendships`):** Al eliminar cuenta en `DeleteAccountUseCase`, buscar con `whereArrayContains("memberIDs", userId)` y eliminar todos los documentos en `friendships` para no dejar amistades huérfanas en el feed de los amigos.
- **Borrar Caprichos (`caprices`):** Borrar también las encuestas creadas por el usuario (`whereEqualTo("userId", userId)`).

### C. Deuda Técnica y Contrato con iOS:
- **Fechas en `Caprice.kt`:** Cambiar `timestamp: Long` por `Date` para cumplir con las directrices de Firestore e iOS.
- **Limpieza de archivos:** Renombrar `application/usecase/auth/AuthViewModel.kt` a `AuthUseCases.kt` (o dividir en use cases) y eliminar `FirebaseAuthManager.kt` (código muerto).
- **Protección en `SendFriendRequestUseCase`:** Comprobar si ya existe amistad previa antes de hacer `.set()` para evitar sobreescribir un estado `ACCEPTED`.
- **Gestión de Amistades:** Añadir opción para eliminar amigos existentes ("unfriend").

### D. Reorganización de Historial Git (Requisito Académico):
- Reconstruir el historial lineal en ramas de características temáticas (GitFlow: e.g. `feat/auth`, `feat/friendship`, `feat/caprice`) mediante cherry-pick seguro manteniendo un branch de respaldo intocable.
