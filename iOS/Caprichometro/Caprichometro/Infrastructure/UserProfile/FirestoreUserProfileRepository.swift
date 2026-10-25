import Foundation
import FirebaseFirestore

@MainActor
final class FirestoreUserProfileRepository: UserProfileRepository {
    private lazy var database = Firestore.firestore()

    private static let transactionErrorDomain =
        "Caprichometro.UserProfileRepository"

    private static let usernameTakenCode = 1

    func isUsernameAvailable(_ username: String) async throws -> Bool {
        let snapshot = try await database
            .collection("usernames")
            .document(username)
            .getDocument()

        return !snapshot.exists
    }

    func save(_ profile: UserProfile) async throws {
        let profileReference = database
            .collection("users")
            .document(profile.id)

        let usernameReference = database
            .collection("usernames")
            .document(profile.username)

        let profileData: [String: Any] = [
            "username": profile.username,
            "createdAt": profile.createdAt
        ]

        do {
            _ = try await database.runTransaction {
                transaction, errorPointer -> Any? in

                let usernameSnapshot: DocumentSnapshot

                do {
                    usernameSnapshot = try transaction.getDocument(
                        usernameReference
                    )
                } catch let readError as NSError {
                    errorPointer?.pointee = readError
                    return nil
                }

                if usernameSnapshot.exists {
                    let existingUserID =
                        usernameSnapshot.data()?["uid"] as? String

                    guard existingUserID == profile.id else {
                        errorPointer?.pointee = NSError(
                            domain: Self.transactionErrorDomain,
                            code: Self.usernameTakenCode,
                            userInfo: [
                                NSLocalizedDescriptionKey:
                                    "Ese nombre de usuario ya está ocupado."
                            ]
                        )
                        return nil
                    }
                }

                transaction.setData(
                    profileData,
                    forDocument: profileReference,
                    merge: true
                )

                transaction.setData(
                    ["uid": profile.id],
                    forDocument: usernameReference
                )

                return nil
            }
        } catch {
            let transactionError = error as NSError

            if transactionError.domain == Self.transactionErrorDomain,
               transactionError.code == Self.usernameTakenCode {
                throw UserProfileRepositoryError.usernameAlreadyTaken
            }

            throw error
        }
    }
}
