# 🟢 Caprichómetro para iOS

> Tus caprichos, tus dudas y tus planes en 5 minutos.

## ✨ Últimas funciones implementadas

- Registro e inicio de sesión con Firebase Authentication mediante correo y contraseña, además de acceso con Google.
- Registro con nombre de usuario único (`@usuario`). La disponibilidad se comprueba y se guarda junto al perfil en Firestore.
- Búsqueda de usuarios por nombre de usuario y envío de solicitudes de amistad.
- Pantalla de amigos organizada en este orden: buscador, solicitudes pendientes y lista completa de amistades aceptadas.
- Las solicitudes se pueden aceptar o rechazar. Al aceptar una solicitud, la amistad aparece en la lista de amigos.
- Creación de caprichos con nombre y precio, asociados al usuario autenticado y guardados en Firestore.

## 📱 Estado actual de iOS

La aplicación iOS ya permite autenticarse, crear perfiles con nombres de usuario, buscar otros perfiles, gestionar solicitudes de amistad y guardar caprichos en la nube.

La cuenta atrás de cinco minutos, las votaciones de los amigos y la pantalla de resultados todavía no están implementadas en esta versión iOS.

## 🏗 Arquitectura y estructura del proyecto

La aplicación separa las reglas de negocio de SwiftUI y Firebase mediante tres capas:

- **Dominio (`Domain`):** modelos y contratos de repositorio. No depende de SwiftUI ni de Firebase.
- **Aplicación (`Application`):** casos de uso que coordinan las operaciones de dominio.
- **Infraestructura (`Infrastructure`):** vistas SwiftUI, ViewModels, adaptadores de Firebase, composición de dependencias e inicio de la aplicación.

```text
iOS/Caprichometro/Caprichometro/
├── Domain/
│   ├── Authentication/
│   ├── Caprice/
│   ├── Friendship/
│   │   └── LoadIncomingFriendRequestsUseCase.swift
│   └── UserProfile/
├── Application/
│   ├── Authentication/
│   ├── Caprice/
│   ├── Friendship/
│   └── UserProfile/
└── Infrastructure/
    ├── App/
    │   ├── Bootstrap/
    │   └── Composition/
    ├── Authentication/
    ├── Caprice/
    ├── Presentation/
    └── UserProfile/
```

### Dominio (`Domain`)

- `Caprice`, `UserProfile` y `Friendship` representan los datos del negocio.
- `AuthRepository`, `CapriceRepository`, `UserProfileRepository` y `FriendshipRepository` definen los puertos que necesita la aplicación.
- `UsernameRules` normaliza y valida los nombres de usuario.

### Aplicación (`Application`)

- `AuthUseCases` coordina las operaciones de autenticación.
- `RegisterUserUseCase` valida el nombre de usuario, registra la cuenta y guarda su perfil.
- `SaveCapriceUseCase` prepara y persiste un capricho.
- `SearchUserByUsernameUseCase`, `SendFriendRequestUseCase` y `RespondToFriendRequestUseCase` implementan los flujos de perfiles y amistades.
- `LoadIncomingFriendRequestsUseCase` carga solicitudes y amistades aceptadas. Actualmente está ubicado en `Domain/Friendship`, aunque por responsabilidad corresponde a la capa `Application`.

### Infraestructura (`Infrastructure`)

- **Autenticación:** `FirebaseAuthRepository` adapta Firebase Authentication; `SocialSignIn` integra el acceso con Google.
- **Perfiles:** `FirestoreUserProfileRepository` guarda y busca perfiles e índices de nombres de usuario.
- **Amistades:** `FirestoreFriendshipRepository` guarda solicitudes y consulta relaciones pendientes y aceptadas.
- **Caprichos:** `FirestoreCapriceRepository` guarda los caprichos en Cloud Firestore.
- **Presentación:** las vistas SwiftUI muestran la interfaz y los ViewModels coordinan la interacción con los casos de uso.
- **Composición:** las clases de `Infrastructure/App/Composition` construyen las dependencias de cada flujo.
- **Inicio:** `AppDelegate` inicializa Firebase al arrancar la aplicación.

## 🔄 Flujos implementados

### Registro e inicio de sesión

1. `AuthView` recoge los datos del usuario.
2. `AuthViewModel` valida los campos y coordina el envío.
3. `RegisterUserUseCase` normaliza y valida el nombre de usuario, comprueba su disponibilidad y registra la cuenta.
4. `FirebaseAuthRepository` se comunica con Firebase Authentication.
5. `FirestoreUserProfileRepository` guarda el perfil y reserva el nombre de usuario.

### Solicitudes y amigos

1. `SearchUserView` busca un nombre de usuario y permite enviar una solicitud.
2. `FirestoreFriendshipRepository` guarda la relación en `friendships` con estado `pending`.
3. Las solicitudes entrantes aparecen debajo del buscador y se pueden aceptar o rechazar.
4. Las relaciones con estado `accepted` aparecen en la lista de amigos.

### Creación de caprichos

1. `CreateCapriceView` recoge el nombre y el precio.
2. `CreateCapriceViewModel` valida los datos y obtiene el UID de la sesión activa.
3. `SaveCapriceUseCase` construye el modelo de dominio.
4. `FirestoreCapriceRepository` guarda el documento en Firestore.

## ☁️ Datos guardados en Firestore

- `users/{uid}`: nombre de usuario y fecha de creación del perfil.
- `usernames/{username}`: UID asociado al nombre de usuario, para comprobar su disponibilidad y buscar perfiles.
- `friendships/{uid1_uid2}`: dos UID ordenados, `requestedBy`, estado y marcas de tiempo.
- `caprices/{documentId}`: nombre, precio, tipo, fecha y UID del propietario.

Los estados de una relación son `pending`, `accepted` y `declined`.

## 🧰 Requisitos para abrir el proyecto

- Xcode y el proyecto `iOS/Caprichometro/Caprichometro.xcodeproj`.
- Un proyecto Firebase configurado para la aplicación iOS.
- `GoogleService-Info.plist` añadido al target de la aplicación.
- Firebase Authentication con correo y contraseña activado. Para Google Sign-In, también se necesita habilitar Google y configurar su esquema URL.
- Cloud Firestore habilitado y reglas que permitan las operaciones requeridas por usuarios autenticados.

## 🤖 Reglas de desarrollo

1. **Mantener la arquitectura hexagonal.** `Domain` y `Application` no deben importar Firebase, SwiftUI ni UIKit.
2. **Respetar la dirección de dependencias.** La presentación llama a casos de uso; los casos de uso dependen de protocolos del dominio; los adaptadores de infraestructura implementan esos protocolos.
3. **Mantener Firebase en infraestructura.** No usar `Auth.auth()` ni `Firestore.firestore()` directamente desde las vistas o los casos de uso.
4. **Gestionar el estado de interfaz en ViewModels.** Las vistas SwiftUI muestran el estado y envían acciones; los ViewModels coordinan tareas asíncronas y mensajes para la interfaz.
5. **Usar nombres de código en inglés.** Clases, propiedades, métodos y archivos siguen nomenclatura en inglés; los textos de la interfaz se muestran en español.
6. **Validar datos externos.** Comprobar campos y tipos al leer documentos de Firestore y traducir errores del SDK a errores entendibles por la aplicación.
7. **Componer dependencias en `Infrastructure/App/Composition`.** Evitar crear repositorios Firebase directamente desde una vista.
