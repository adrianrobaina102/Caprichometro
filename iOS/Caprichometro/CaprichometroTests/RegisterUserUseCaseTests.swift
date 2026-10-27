import XCTest
@testable import Caprichometro

@MainActor
final class RegisterUserUseCaseTests: XCTestCase {
    func testInvalidUsernameStopsBeforeCheckingAvailabilityOrRegistering() async {
        let authRepository = TestAuthRepository()
        let profileRepository = TestUserProfileRepository()
        let useCase = makeUseCase(
            authRepository: authRepository,
            profileRepository: profileRepository
        )

        do {
            try await useCase.execute(
                username: "ad",
                email: "dani@example.com",
                password: "password123"
            )
            XCTFail("Expected an invalid username error.")
        } catch RegisterUserError.invalidUsername {
            XCTAssertTrue(profileRepository.checkedUsernames.isEmpty)
            XCTAssertEqual(authRepository.registerCallCount, 0)
        } catch {
            XCTFail("Unexpected error: \(error)")
        }
    }

    func testTakenUsernameStopsBeforeRegistering() async {
        let authRepository = TestAuthRepository()
        let profileRepository = TestUserProfileRepository()
        profileRepository.usernameIsAvailable = false
        let useCase = makeUseCase(
            authRepository: authRepository,
            profileRepository: profileRepository
        )

        do {
            try await useCase.execute(
                username: "adrian_dev",
                email: "dani@example.com",
                password: "password123"
            )
            XCTFail("Expected a username-already-taken error.")
        } catch UserProfileRepositoryError.usernameAlreadyTaken {
            XCTAssertEqual(profileRepository.checkedUsernames, ["adrian_dev"])
            XCTAssertEqual(authRepository.registerCallCount, 0)
        } catch {
            XCTFail("Unexpected error: \(error)")
        }
    }

    func testAuthenticationFailureDoesNotSaveProfile() async {
        let authRepository = TestAuthRepository()
        authRepository.registerError = TestFailure.expected
        let profileRepository = TestUserProfileRepository()
        let useCase = makeUseCase(
            authRepository: authRepository,
            profileRepository: profileRepository
        )

        do {
            try await useCase.execute(
                username: "adrian_dev",
                email: "dani@example.com",
                password: "password123"
            )
            XCTFail("Expected the authentication error to be propagated.")
        } catch TestFailure.expected {
            XCTAssertTrue(profileRepository.savedProfiles.isEmpty)
        } catch {
            XCTFail("Unexpected error: \(error)")
        }
    }

    func testSuccessfulRegistrationSavesNormalizedProfile() async throws {
        let authRepository = TestAuthRepository()
        authRepository.registeredUserID = "uid-123"
        let profileRepository = TestUserProfileRepository()
        let useCase = makeUseCase(
            authRepository: authRepository,
            profileRepository: profileRepository
        )

        let startedAt = Date()
        try await useCase.execute(
            username: "  @Adrian_Dev  ",
            email: "  dani@example.com  ",
            password: "password123"
        )

        XCTAssertEqual(profileRepository.checkedUsernames, ["adrian_dev"])
        XCTAssertEqual(authRepository.registerCallCount, 1)
        XCTAssertEqual(authRepository.lastRegisteredEmail, "dani@example.com")
        XCTAssertEqual(authRepository.lastRegisteredPassword, "password123")
        XCTAssertEqual(profileRepository.savedProfiles.count, 1)
        XCTAssertEqual(profileRepository.savedProfiles.first?.id, "uid-123")
        XCTAssertEqual(profileRepository.savedProfiles.first?.username, "adrian_dev")
        XCTAssertGreaterThanOrEqual(
            profileRepository.savedProfiles.first?.createdAt ?? .distantPast,
            startedAt
        )
    }

    private func makeUseCase(
        authRepository: TestAuthRepository,
        profileRepository: TestUserProfileRepository
    ) -> RegisterUserUseCase {
        let authUseCases = AuthUseCases(repository: authRepository)
        return RegisterUserUseCase(
            authUseCases: authUseCases,
            userProfileRepository: profileRepository
        )
    }
}
