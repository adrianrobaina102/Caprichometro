import Foundation
import UIKit
import GoogleSignIn
import FirebaseCore

/// Adaptador de presentación de los proveedores externos.
@MainActor
final class SocialSignIn {
    func google() async throws -> SocialCredential {
        guard let options = FirebaseApp.app()?.options,
              let clientID = options.clientID, !clientID.isEmpty,
              options.bundleID == Bundle.main.bundleIdentifier else {
            throw AuthenticationError.providerNotConfigured
        }
        let scheme = clientID.split(separator: ".").reversed().joined(separator: ".")
        let urlTypes = Bundle.main.object(forInfoDictionaryKey: "CFBundleURLTypes") as? [[String: Any]] ?? []
        let schemes = urlTypes.flatMap { $0["CFBundleURLSchemes"] as? [String] ?? [] }
        guard schemes.contains(scheme) else {
            throw AuthenticationError.providerNotConfigured
        }
        let presenter = try presentingController()
        GIDSignIn.sharedInstance.configuration = GIDConfiguration(clientID: clientID)
        do {
            let result = try await GIDSignIn.sharedInstance.signIn(withPresenting: presenter)
            guard let idToken = result.user.idToken?.tokenString else {
                throw AuthenticationError.invalidCredentials
            }
            return .google(idToken: idToken, accessToken: result.user.accessToken.tokenString)
        } catch {
            let nsError = error as NSError
            if nsError.domain == kGIDSignInErrorDomain,
               nsError.code == GIDSignInError.canceled.rawValue {
                throw AuthenticationError.cancelled
            }
            throw error
        }
    }

    private func presentingController() throws -> UIViewController {
        let windows = UIApplication.shared.connectedScenes
            .compactMap { $0 as? UIWindowScene }
            .filter { $0.activationState == .foregroundActive }
            .flatMap(\.windows)
            .filter(\.isKeyWindow)
        // No seleccionar una ventana arbitraria si hay varias escenas activas.
        guard windows.count == 1, var controller = windows.first?.rootViewController else {
            throw AuthenticationError.presentationUnavailable
        }
        while let presented = controller.presentedViewController {
            controller = presented
        }
        return controller
    }
}
