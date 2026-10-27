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
