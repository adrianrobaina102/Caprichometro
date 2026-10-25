import Foundation
import Observation

@MainActor
@Observable
final class AuthViewModel {
    enum Field: Hashable {
        case username
        case email
        case password
    }

    var username = ""
    var birthday: Date?
    var email = ""
    var password = ""
    private(set) var isSubmitting = false
    private(set) var isLoggedIn = false
    private(set) var isCheckingSession = true
    private(set) var errorMessage: String?
    private(set) var invalidField: Field?
    private(set) var signOutError: String?
    private let useCases: AuthUseCases
    private let registerUserUseCase: RegisterUserUseCase

    init(
        useCases: AuthUseCases,
        registerUserUseCase: RegisterUserUseCase
    ) {
        self.useCases = useCases
        self.registerUserUseCase = registerUserUseCase
    }
    var showsSignOutError: Bool {
        get { signOutError != nil }
        set { if !newValue { signOutError = nil } }
    }

    func startObservingSession() {
        do {
            try useCases.observeSession { [weak self] isLoggedIn in
                self?.isLoggedIn = isLoggedIn
                self?.isCheckingSession = false
            }
        } catch {
            isCheckingSession = false
            errorMessage = authenticationMessage(for: error)
        }
    }

    func stopObservingSession() {
        useCases.stopObservingSession()
    }

    func clearError() {
        errorMessage = nil
        invalidField = nil
    }

    func validate(isRegistering: Bool) -> Bool {
        clearError()
        if isRegistering {
            guard UsernameRules.isValid(username) else {
                errorMessage = "Usa de 3 a 20 letras, números o guiones bajos."
                invalidField = .username
                return false
            }
            guard let birthday, birthday <= Date() else {
                errorMessage = "Selecciona tu fecha de nacimiento."
                invalidField = nil
                return false
            }
        }
        guard !email.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else {
            errorMessage = "Introduce tu correo electrónico."
            invalidField = .email
            return false
        }
        guard !password.isEmpty else {
            errorMessage = "Introduce tu contraseña."
            invalidField = .password
            return false
        }
        errorMessage = nil
        invalidField = nil
        return true
    }

    func signIn() async {
        await authenticate(isRegistering: false)
    }

    func register() async {
        await authenticate(isRegistering: true)
    }

    private func authenticate(isRegistering: Bool) async {
        guard !isSubmitting, validate(isRegistering: isRegistering) else { return }
        let submittedEmail = email.trimmingCharacters(in: .whitespacesAndNewlines)
        let submittedPassword = password
        isSubmitting = true
        defer { isSubmitting = false }

        do {
            if isRegistering {
                try await registerUserUseCase.execute(
                    username: username,
                    email: submittedEmail,
                    password: submittedPassword
                )
            } else {
                try await useCases.signIn(
                    email: submittedEmail,
                    password: submittedPassword
                )
            }
            password = ""
        } catch {
            errorMessage = authenticationMessage(for: error)
        }
    }

    func signInWithGoogle() async {
        await signIn(provider: .google)
    }

    private func signIn(provider: SocialProvider) async {
        guard !isSubmitting else { return }
        clearError()
        isSubmitting = true
        defer { isSubmitting = false }
        do {
            try await useCases.signIn(provider: provider)
            password = ""
        } catch AuthenticationError.cancelled {
            // Cancelar el selector no es un fallo de autenticación.
        } catch {
            errorMessage = authenticationMessage(for: error)
        }
    }

    func signOut() {
        do {
            try useCases.signOut()
            username = ""
            birthday = nil
            email = ""
            password = ""
            clearError()
            signOutError = nil
        } catch {
            signOutError = "No se pudo cerrar la sesión. Inténtalo de nuevo."
        }
    }

    private func authenticationMessage(for error: Error) -> String {
        if let error = error as? RegisterUserError {
            switch error {
            case .invalidUsername:
                return "Usa de 3 a 20 letras, números o guiones bajos."
            }
        }

        if let error = error as? UserProfileRepositoryError {
            switch error {
            case .usernameAlreadyTaken:
                return "Ese nombre de usuario ya está ocupado."
            case .invalidUserProfileData:
                return "No se pudieron leer los datos del perfil. Inténtalo de nuevo."
            }
        }
        switch (error as? AuthenticationError) ?? .unknown {
        case .invalidEmail:
            return "Introduce un correo electrónico válido."
        case .invalidCredentials:
            return "El correo o la contraseña no son correctos."
        case .emailAlreadyInUse:
            return "Ese correo ya tiene una cuenta. Inicia sesión."
        case .weakPassword:
            return "La contraseña no cumple los requisitos. Utiliza al menos 6 caracteres."
        case .network:
            return "No se pudo conectar. Comprueba tu conexión a internet."
        case .tooManyRequests:
            return "Demasiados intentos. Espera un momento y vuelve a intentarlo."
        case .userDisabled:
            return "Esta cuenta está deshabilitada."
        case .operationNotAllowed:
            return "Este método de acceso no está habilitado en Firebase."
        case .notConfigured:
            return "Firebase no está inicializado."
        case .cancelled:
            return ""
        case .providerNotConfigured:
            return "Google no está configurado. Revisa el archivo de Firebase y el esquema URL de la app."
        case .presentationUnavailable:
            return "No se pudo abrir el acceso. Vuelve a intentarlo desde la pantalla principal."
        case .accountExists:
            return "Ya existe una cuenta con este correo. Inicia sesión con el método que utilizaste al registrarte."
        case .unknown:
            return "No se pudo completar la operación. Inténtalo de nuevo."
            
        }
        
    }
}
