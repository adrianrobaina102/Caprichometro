import Foundation

enum RegisterUserError: Error {
    case invalidUsername
}

@MainActor
struct RegisterUserUseCase {
    private let authUseCases: AuthUseCases
    private let userProfileRepository: any UserProfileRepository

    init(
        authUseCases: AuthUseCases,
        userProfileRepository: any UserProfileRepository
    ) {
        self.authUseCases = authUseCases
        self.userProfileRepository = userProfileRepository
    }

    func execute(
        username: String,
        email: String,
        password: String
    ) async throws {
        let normalizedUsername = UsernameRules.normalize(username)

        guard UsernameRules.isValid(normalizedUsername) else {
            throw RegisterUserError.invalidUsername
        }

        guard try await userProfileRepository
            .isUsernameAvailable(normalizedUsername) else {
            throw UserProfileRepositoryError.usernameAlreadyTaken
        }

        let userID = try await authUseCases.register(
            email: email,
            password: password
        )

        let profile = UserProfile(
            id: userID,
            username: normalizedUsername,
            createdAt: Date()
        )

        try await userProfileRepository.save(profile)
    }
}
