import Foundation
@testable import Caprichometro

enum TestFailure: Error {
    case expected
}

@MainActor
final class TestAuthRepository: AuthRepository {
    var signInError: Error?
    var registerError: Error?
    var providerSignInError: Error?
    var signOutError: Error?
    var observeSessionError: Error?
    var registeredUserID = "test-user-id"

    private(set) var signInCallCount = 0
    private(set) var registerCallCount = 0
    private(set) var providerSignInCallCount = 0
    private(set) var didRequestGoogleSignIn = false
    private(set) var lastSignInEmail: String?
    private(set) var lastSignInPassword: String?
    private(set) var lastRegisteredEmail: String?
    private(set) var lastRegisteredPassword: String?

    private var sessionObserver: (@MainActor (Bool) -> Void)?

    func signIn(email: String, password: String) async throws {
        signInCallCount += 1
        lastSignInEmail = email
        lastSignInPassword = password

        if let signInError {
            throw signInError
        }
    }

    func register(email: String, password: String) async throws -> String {
        registerCallCount += 1
        lastRegisteredEmail = email
        lastRegisteredPassword = password

        if let registerError {
            throw registerError
        }

        return registeredUserID
    }

    func signIn(provider: SocialProvider) async throws {
        providerSignInCallCount += 1
        if case .google = provider {
            didRequestGoogleSignIn = true
        }

        if let providerSignInError {
            throw providerSignInError
        }
    }

    func signOut() throws {
        if let signOutError {
            throw signOutError
        }
    }

    func observeSession(
        _ onChange: @escaping @MainActor (Bool) -> Void
    ) throws {
        if let observeSessionError {
            throw observeSessionError
        }

        sessionObserver = onChange
    }

    func stopObservingSession() {
        sessionObserver = nil
    }
}

@MainActor
final class TestUserProfileRepository: UserProfileRepository {
    var usernameIsAvailable = true
    var usernameAvailabilityError: Error?
    var profileSaveError: Error?
    var profileLookupError: Error?
    var usernameSearchResult: UserProfile?
    var userIDSearchResult: UserProfile?

    private(set) var checkedUsernames: [String] = []
    private(set) var savedProfiles: [UserProfile] = []

    func isUsernameAvailable(_ username: String) async throws -> Bool {
        checkedUsernames.append(username)

        if let usernameAvailabilityError {
            throw usernameAvailabilityError
        }

        return usernameIsAvailable
    }

    func findByUsername(_ username: String) async throws -> UserProfile? {
        if let profileLookupError {
            throw profileLookupError
        }

        return usernameSearchResult
    }

    func save(_ profile: UserProfile) async throws {
        if let profileSaveError {
            throw profileSaveError
        }

        savedProfiles.append(profile)
    }

    func findByID(_ userID: String) async throws -> UserProfile? {
        if let profileLookupError {
            throw profileLookupError
        }

        guard userIDSearchResult?.id == userID else {
            return nil
        }

        return userIDSearchResult
    }
}

@MainActor
final class TestCapriceRepository: CapriceRepository {
    var documentID = "test-caprice-id"
    var saveError: Error?

    private(set) var savedCaprices: [Caprice] = []

    func save(_ caprice: Caprice) async throws -> String {
        if let saveError {
            throw saveError
        }

        savedCaprices.append(caprice)
        return documentID
    }
}
