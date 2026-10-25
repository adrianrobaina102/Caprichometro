/// Punto de composición: aquí se elige el adaptador de producción.
@MainActor
enum AuthComposition {
    static func makeViewModel() -> AuthViewModel {
        AuthViewModel(useCases: AuthUseCases(repository: FirebaseAuthRepository()))
    }

    static func makePreviewViewModel() -> AuthViewModel {
        AuthViewModel(useCases: AuthUseCases(repository: PreviewAuthRepository()))
    }
}

/// Adaptador en memoria para que las previews no accedan a Firebase.
@MainActor
private final class PreviewAuthRepository: AuthRepository {
    private var onChange: (@MainActor (Bool) -> Void)?

    func signIn(email: String, password: String) async throws { onChange?(true) }
    func register(email: String, password: String) async throws { onChange?(true) }
    func signIn(provider: SocialProvider) async throws { onChange?(true) }
    func signOut() throws { onChange?(false) }
    func observeSession(_ onChange: @escaping @MainActor (Bool) -> Void) throws {
        self.onChange = onChange
        onChange(false)
    }
    func stopObservingSession() { onChange = nil }
}
