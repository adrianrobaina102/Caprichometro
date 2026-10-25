import Foundation

/// Casos de uso de autenticación, independientes del proveedor.
@MainActor
struct AuthUseCases {
    private let repository: any AuthRepository

    init(repository: any AuthRepository) {
        self.repository = repository
    }

    func signIn(email: String, password: String) async throws {
        try await repository.signIn(email: email.trimmingCharacters(in: .whitespacesAndNewlines), password: password)
    }

    func register(email: String, password: String) async throws -> String {
        try await repository.register(
            email: email.trimmingCharacters(in: .whitespacesAndNewlines),
            password: password
        )
    }

    func signIn(provider: SocialProvider) async throws {
        try await repository.signIn(provider: provider)
    }

    func signOut() throws {
        try repository.signOut()
    }

    func observeSession(_ onChange: @escaping @MainActor (Bool) -> Void) throws {
        try repository.observeSession(onChange)
    }

    func stopObservingSession() {
        repository.stopObservingSession()
    }
}
