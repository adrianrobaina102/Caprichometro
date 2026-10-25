# 🟢 Caprichómetro

> Tus caprichos, tus dudas y tus planes en 5 minutos.

Caprichómetro es una aplicación Android nativa diseñada para ayudar a los usuarios a gestionar sus decisiones, caprichos y planes de forma rápida e intuitiva. El proyecto destaca por estar construido bajo estrictos estándares de código limpio y separación de responsabilidades.

## 🏗 Arquitectura: Hexagonal (Ports and Adapters)

El proyecto está estructurado utilizando **Arquitectura Hexagonal** (Clean Architecture), garantizando que la lógica de negocio esté completamente aislada de los frameworks externos y la interfaz de usuario. El código se divide en tres capas principales:

-   **Dominio (Domain):** Contiene los modelos de negocio puros (como `User` o los datos manejados con `LocalDate`) y los puertos (interfaces) que definen cómo la aplicación se comunica con el exterior. No tiene dependencias de Android.
-   **Aplicación (Application):** Contiene los Casos de Uso (`SignInUseCase`, `RegisterUseCase`, `SaveCapriceUseCase`, `SignInWithGoogleUseCase`) que orquestan la lógica de la aplicación utilizando los puertos del dominio.
-   **Infraestructura (Infrastructure):** Contiene los adaptadores tecnológicos.
    -   **UI:** Implementada 100% con Jetpack Compose y ViewModels (MVI/MVVM).
    -   **Data:** Implementaciones reales de los repositorios utilizando Firebase Authentication y Cloud Firestore (`FirebaseAuthRepository`, `FirestoreCapriceRepository`).

## 🛠 Stack Tecnológico

-   **Lenguaje:** Kotlin
-   **UI:** Jetpack Compose (Material Design 3)
-   **Arquitectura:** Clean Architecture + MVVM + StateFlow
-   **Fechas:** API moderna `java.time` (`LocalDate`, `Instant`)
-   **Backend as a Service (BaaS):** Firebase (Auth & Firestore)
-   **Testing:** JUnit 5, MockK, AssertJ
-   **Control de Versiones:** Git

## ✅ Estado Actual (Progreso)

La base del proyecto está asentada y actualmente contamos con:

- [x] **Estructura Hexagonal:** Capas definidas e inyección de dependencias manual preparada.
- [x] **Sistema de Autenticación (Email/Password):** Registro e inicio de sesión integrados con Firebase Auth.
- [x] **Inicio de sesión con Google (SSO):** Integración nativa con la API moderna `Credential Manager` y Firebase Auth, ocultando credenciales seguras mediante `BuildConfig`.
- [x] **Interfaz de Usuario (AuthScreen):** UI personalizada con tema oscuro, acentos en verde lima y diseño adaptativo.
- [x] **Validaciones Robustas:** Comprobación de formato de email, longitud de contraseña y campos vacíos.
- [x] **Selector de Fecha (DatePicker):** Integración del calendario nativo de Material 3 con blindaje lógico.
- [x] **Enrutamiento por Estado:** Navegación basada en estado (`isAuthSuccessful`) desde la pantalla de login hacia la pantalla principal (`CapriceScreen`).
- [x] **Testing Unitario:** Cobertura de la capa de Presentación (`AuthViewModel`) usando JUnit 5, MockK y AssertJ para gestionar estados con corrutinas de prueba.

## 🚀 Próximos Pasos (TODO)

- [ ] **Desarrollo de `CapriceScreen`:** Implementar la interfaz principal y la lógica core de la aplicación para guardar y leer los "caprichos" en Firestore.
- [ ] **Navegación Avanzada:** Implementar *Navigation Compose* para gestionar el backstack cuando el flujo de pantallas crezca.
- [ ] **Testing de Interfaz (UI):** Escribir tests de interfaz gráfica para los componentes de Jetpack Compose.
- [ ] **Inyección de Dependencias (DI):** Migrar la creación manual de repositorios a Hilt o Koin para mayor escalabilidad.