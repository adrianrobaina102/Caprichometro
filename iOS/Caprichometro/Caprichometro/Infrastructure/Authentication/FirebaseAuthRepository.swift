import Foundation
import FirebaseAuth
import FirebaseCore
import GoogleSignIn

/// Adaptador de salida que traduce las operaciones y errores de Firebase.
@MainActor
final class FirebaseAuthRepository: AuthRepository {
    private let socialSignIn = SocialSignIn()
    private var listener: AuthStateDidChangeListenerHandle?
    private var observedAuth: Auth?

    private func configuredAuth() throws -> Auth {
        guard FirebaseApp.app() != nil else {
            throw AuthenticationError.notConfigured
        }
        return Auth.auth()
    }

    func signIn(email: String, password: String) async throws {
        do {
            _ = try await configuredAuth().signIn(withEmail: email, password: password)
        } catch {
            throw mapError(error)
        }
    }

    func register(email: String, password: String) async throws -> String {
        do {
            let result = try await configuredAuth()
                .createUser(withEmail: email, password: password)

            return result.user.uid
        } catch {
            throw mapError(error)
        }
    }
    func signIn(provider: SocialProvider) async throws {
        do {
            let auth = try configuredAuth()
            let socialCredential: SocialCredential
            switch provider {
            case .google: socialCredential = try await socialSignIn.google()
            }
            let credential: AuthCredential
            switch socialCredential {
            case let .google(idToken, accessToken):
                credential = GoogleAuthProvider.credential(withIDToken: idToken, accessToken: accessToken)
            }
            _ = try await auth.signIn(with: credential)
        } catch {
            throw mapError(error)
        }
    }

    func signOut() throws {
        do {
            try configuredAuth().signOut()
            GIDSignIn.sharedInstance.signOut()
        } catch {
            throw mapError(error)
        }
    }

    func observeSession(_ onChange: @escaping @MainActor (Bool) -> Void) throws {
        guard listener == nil else { return }
        let auth = try configuredAuth()
        observedAuth = auth
        listener = auth.addStateDidChangeListener { _, user in
            onChange(user != nil)
        }
    }

    func stopObservingSession() {
        if let listener {
            observedAuth?.removeStateDidChangeListener(listener)
        }
        listener = nil
        observedAuth = nil
    }

    private func mapError(_ error: Error) -> AuthenticationError {
        if let error = error as? AuthenticationError { return error }
        guard (error as NSError).domain == AuthErrorDomain else {
            return (error as NSError).domain == NSURLErrorDomain ? .network : .unknown
        }
        switch AuthErrorCode(rawValue: (error as NSError).code) {
        case .accountExistsWithDifferentCredential: return .accountExists
        case .invalidEmail: return .invalidEmail
        case .wrongPassword, .userNotFound, .invalidCredential: return .invalidCredentials
        case .emailAlreadyInUse: return .emailAlreadyInUse
        case .weakPassword: return .weakPassword
        case .networkError: return .network
        case .tooManyRequests: return .tooManyRequests
        case .userDisabled: return .userDisabled
        case .operationNotAllowed: return .operationNotAllowed
        default: return .unknown
        }
    }
}
