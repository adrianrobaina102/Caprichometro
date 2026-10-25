@MainActor
enum AuthComposition {
    static func makeViewModel() -> AuthViewModel {
        let authRepository = FirebaseAuthRepository()
        let authUseCases = AuthUseCases(repository: authRepository)
        let profileRepository = FirestoreUserProfileRepository()

        let registerUserUseCase = RegisterUserUseCase(
            authUseCases: authUseCases,
            userProfileRepository: profileRepository
        )

        return AuthViewModel(
            useCases: authUseCases,
            registerUserUseCase: registerUserUseCase
        )
    }

    static func makePreviewViewModel() -> AuthViewModel {
        let authUseCases = AuthUseCases(repository: PreviewAuthRepository())

        let registerUserUseCase = RegisterUserUseCase(
            authUseCases: authUseCases,
            userProfileRepository: PreviewUserProfileRepository()
        )

        return AuthViewModel(
            useCases: authUseCases,
            registerUserUseCase: registerUserUseCase
        )
    }
}

@MainActor
private final class PreviewAuthRepository: AuthRepository {
    private var onChange: (@MainActor (Bool) -> Void)?

    func signIn(email: String, password: String) async throws {
        onChange?(true)
    }

    func register(email: String, password: String) async throws -> String {
        onChange?(true)
        return "preview-user-id"
    }

    func signIn(provider: SocialProvider) async throws {
        onChange?(true)
    }

    func signOut() throws {
        onChange?(false)
    }

    func observeSession(
        _ onChange: @escaping @MainActor (Bool) -> Void
    ) throws {
        self.onChange = onChange
        onChange(false)
    }

    func stopObservingSession() {
        onChange = nil
    }
}

@MainActor
private final class PreviewUserProfileRepository: UserProfileRepository {
    func save(_ profile: UserProfile) async throws {}
    func isUsernameAvailable(_ username: String) async throws -> Bool {
        true
    }
}
