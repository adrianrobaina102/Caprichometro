import Foundation

enum AuthenticationError: Error {
    case invalidEmail, invalidCredentials, emailAlreadyInUse, weakPassword
    case network, tooManyRequests, userDisabled, operationNotAllowed
    case notConfigured, unknown
    case cancelled, providerNotConfigured, presentationUnavailable, accountExists
}

/// Puerto de salida: el dominio no conoce Firebase ni sus tipos.
@MainActor
protocol AuthRepository: AnyObject {
    func signIn(email: String, password: String) async throws
    func register(email: String, password: String) async throws -> String
    func signIn(provider: SocialProvider) async throws
    func signOut() throws
    func observeSession(_ onChange: @escaping @MainActor (Bool) -> Void) throws
    func stopObservingSession()
}
