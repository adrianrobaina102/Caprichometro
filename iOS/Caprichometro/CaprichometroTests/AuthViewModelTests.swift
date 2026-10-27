import XCTest
@testable import Caprichometro

@MainActor
final class AuthViewModelTests: XCTestCase {
    func testGoogleSignInSuccessFinishesWithoutError() async {
        let repository = TestAuthRepository()
        let viewModel = makeViewModel(repository: repository)

        await viewModel.signInWithGoogle()

        XCTAssertTrue(repository.didRequestGoogleSignIn)
        XCTAssertEqual(repository.providerSignInCallCount, 1)
        XCTAssertFalse(viewModel.isSubmitting)
        XCTAssertNil(viewModel.errorMessage)
    }

    func testGoogleSignInFailureShowsErrorAndFinishesSubmitting() async {
        let repository = TestAuthRepository()
        repository.providerSignInError = AuthenticationError.network
        let viewModel = makeViewModel(repository: repository)

        await viewModel.signInWithGoogle()

        XCTAssertFalse(viewModel.isSubmitting)
        XCTAssertEqual(
            viewModel.errorMessage,
            "No se pudo conectar. Comprueba tu conexión a internet."
        )
    }

    func testGoogleSignInCancellationDoesNotShowError() async {
        let repository = TestAuthRepository()
        repository.providerSignInError = AuthenticationError.cancelled
        let viewModel = makeViewModel(repository: repository)

        await viewModel.signInWithGoogle()

        XCTAssertFalse(viewModel.isSubmitting)
        XCTAssertNil(viewModel.errorMessage)
    }

    func testRegistrationRejectsInvalidEmailFormat() {
        let viewModel = makeViewModel(repository: TestAuthRepository())
        viewModel.username = "dani"
        viewModel.birthday = Date()
        viewModel.email = "not-an-email"
        viewModel.password = "123456"

        XCTAssertFalse(viewModel.validate(isRegistering: true))
        XCTAssertEqual(viewModel.invalidField, .email)
        XCTAssertEqual(
            viewModel.errorMessage,
            "Introduce un correo electrónico válido."
        )
    }

    func testRegistrationRejectsPasswordShorterThanSixCharacters() {
        let viewModel = makeViewModel(repository: TestAuthRepository())
        viewModel.username = "dani"
        viewModel.birthday = Date()
        viewModel.email = "dani@example.com"
        viewModel.password = "12345"

        XCTAssertFalse(viewModel.validate(isRegistering: true))
        XCTAssertEqual(viewModel.invalidField, .password)
        XCTAssertEqual(
            viewModel.errorMessage,
            "La contraseña debe tener al menos 6 caracteres."
        )
    }

    private func makeViewModel(repository: TestAuthRepository) -> AuthViewModel {
        let useCases = AuthUseCases(repository: repository)
        let registerUserUseCase = RegisterUserUseCase(
            authUseCases: useCases,
            userProfileRepository: TestUserProfileRepository()
        )

        return AuthViewModel(
            useCases: useCases,
            registerUserUseCase: registerUserUseCase
        )
    }
}
