import XCTest
@testable import Caprichometro

@MainActor
final class AuthUseCasesTests: XCTestCase {
    func testSignInPassesTrimmedEmailAndPasswordToRepository() async throws {
        let repository = TestAuthRepository()
        let useCases = AuthUseCases(repository: repository)

        try await useCases.signIn(
            email: "  dani@example.com  ",
            password: "password123"
        )

        XCTAssertEqual(repository.signInCallCount, 1)
        XCTAssertEqual(repository.lastSignInEmail, "dani@example.com")
        XCTAssertEqual(repository.lastSignInPassword, "password123")
    }

    func testSignInPropagatesRepositoryFailure() async {
        let repository = TestAuthRepository()
        repository.signInError = TestFailure.expected
        let useCases = AuthUseCases(repository: repository)

        do {
            try await useCases.signIn(
                email: "dani@example.com",
                password: "wrong-password"
            )
            XCTFail("Expected the repository error to be propagated.")
        } catch TestFailure.expected {
            XCTAssertEqual(repository.signInCallCount, 1)
        } catch {
            XCTFail("Unexpected error: \(error)")
        }
    }
}
