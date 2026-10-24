import Foundation

enum SocialProvider {
    case google
}

/// Datos del proveedor, sin tipos de Firebase del proveedor.
enum SocialCredential {
    case google(idToken: String, accessToken: String)
}
