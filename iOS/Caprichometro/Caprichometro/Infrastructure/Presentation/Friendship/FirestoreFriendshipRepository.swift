//
//  FirestoreFriendshipRepository.swift
//  Caprichometro
//
//  Created by Danii on 25/10/2026.
//

import FirebaseFirestore

@MainActor
final class FirestoreFriendshipRepository: FriendshipRepository {
    private lazy var database = Firestore.firestore()

    private static let transactionErrorDomain =
        "Caprichometro.FriendshipRepository"

    private static let relationshipAlreadyExistsCode = 1
    private static let friendshipNotFoundCode = 2
    private static let cannotRespondCode = 3
    private static let invalidFriendshipDataCode = 4
    func sendRequest(
        from requesterID: String,
        to recipientID: String
    ) async throws {
        let memberIDs = [requesterID, recipientID].sorted()
        let friendshipID = memberIDs.joined(separator: "_")

        let friendshipReference = database
            .collection("friendships")
            .document(friendshipID)

        let requestData: [String: Any] = [
            "memberIDs": memberIDs,
            "requestedBy": requesterID,
            "status": FriendshipStatus.pending.rawValue,
            "createdAt": FieldValue.serverTimestamp(),
            "updatedAt": FieldValue.serverTimestamp()
        ]

        do {
            _ = try await database.runTransaction {
                transaction, errorPointer -> Any? in

                let snapshot: DocumentSnapshot

                do {
                    snapshot = try transaction.getDocument(
                        friendshipReference
                    )
                } catch let readError as NSError {
                    errorPointer?.pointee = readError
                    return nil
                }

                guard !snapshot.exists else {
                    errorPointer?.pointee = NSError(
                        domain: Self.transactionErrorDomain,
                        code: Self.relationshipAlreadyExistsCode,
                        userInfo: [
                            NSLocalizedDescriptionKey:
                                "Ya existe una relación entre estos usuarios."
                        ]
                    )
                    return nil
                }

                transaction.setData(
                    requestData,
                    forDocument: friendshipReference
                )

                return nil
            }
        } catch {
            let transactionError = error as NSError

            if transactionError.domain == Self.transactionErrorDomain,
               transactionError.code == Self.relationshipAlreadyExistsCode {
                throw FriendshipRepositoryError.relationshipAlreadyExists
            }

            throw error
        }
    }
    func incomingRequests(for userID: String) async throws -> [Friendship] {
        let snapshot = try await database
            .collection("friendships")
            .whereField("memberIDs", arrayContains: userID)
            .whereField("status", isEqualTo: FriendshipStatus.pending.rawValue)
            .getDocuments()

        return try snapshot.documents.compactMap { document in
            let data = document.data()

            guard
                let memberIDs = data["memberIDs"] as? [String],
                let requesterID = data["requestedBy"] as? String,
                let statusRawValue = data["status"] as? String,
                let status = FriendshipStatus(rawValue: statusRawValue),
                let createdAt = data["createdAt"] as? Timestamp,
                let updatedAt = data["updatedAt"] as? Timestamp,
                memberIDs.contains(userID),
                memberIDs.contains(requesterID)
            else {
                throw FriendshipRepositoryError.invalidFriendshipData
            }

            guard requesterID != userID else {
                return nil
            }

            return Friendship(
                id: document.documentID,
                memberIDs: memberIDs,
                requestedBy: requesterID,
                status: status,
                createdAt: createdAt.dateValue(),
                updatedAt: updatedAt.dateValue()
            )
        }
    }

    func acceptedFriendships(for userID: String) async throws -> [Friendship] {
        let snapshot = try await database
            .collection("friendships")
            .whereField("memberIDs", arrayContains: userID)
            .whereField("status", isEqualTo: FriendshipStatus.accepted.rawValue)
            .getDocuments()

        return try snapshot.documents.map { document in
            let data = document.data()

            guard
                let memberIDs = data["memberIDs"] as? [String],
                let requesterID = data["requestedBy"] as? String,
                let statusRawValue = data["status"] as? String,
                let status = FriendshipStatus(rawValue: statusRawValue),
                let createdAt = data["createdAt"] as? Timestamp,
                let updatedAt = data["updatedAt"] as? Timestamp,
                memberIDs.count == 2,
                memberIDs.contains(userID),
                memberIDs.contains(requesterID),
                status == .accepted
            else {
                throw FriendshipRepositoryError.invalidFriendshipData
            }

            return Friendship(
                id: document.documentID,
                memberIDs: memberIDs,
                requestedBy: requesterID,
                status: status,
                createdAt: createdAt.dateValue(),
                updatedAt: updatedAt.dateValue()
            )
        }
    }

    func respond(
        to friendshipID: String,
        by userID: String,
        with decision: FriendRequestDecision
    ) async throws {
        let friendshipReference = database
            .collection("friendships")
            .document(friendshipID)

        do {
            _ = try await database.runTransaction {
                transaction, errorPointer -> Any? in

                let snapshot: DocumentSnapshot

                do {
                    snapshot = try transaction.getDocument(friendshipReference)
                } catch let readError as NSError {
                    errorPointer?.pointee = readError
                    return nil
                }

                guard let data = snapshot.data() else {
                    errorPointer?.pointee = NSError(
                        domain: Self.transactionErrorDomain,
                        code: Self.friendshipNotFoundCode,
                        userInfo: nil
                    )
                    return nil
                }

                guard
                    let memberIDs = data["memberIDs"] as? [String],
                    let requesterID = data["requestedBy"] as? String,
                    let status = data["status"] as? String
                else {
                    errorPointer?.pointee = NSError(
                        domain: Self.transactionErrorDomain,
                        code: Self.invalidFriendshipDataCode,
                        userInfo: nil
                    )
                    return nil
                }

                guard
                    memberIDs.count == 2,
                    memberIDs.contains(userID),
                    memberIDs.contains(requesterID),
                    requesterID != userID,
                    status == FriendshipStatus.pending.rawValue
                else {
                    errorPointer?.pointee = NSError(
                        domain: Self.transactionErrorDomain,
                        code: Self.cannotRespondCode,
                        userInfo: nil
                    )
                    return nil
                }

                transaction.updateData(
                    [
                        "status": decision.status.rawValue,
                        "updatedAt": FieldValue.serverTimestamp()
                    ],
                    forDocument: friendshipReference
                )

                return nil
            }
        } catch {
            let transactionError = error as NSError

            guard transactionError.domain == Self.transactionErrorDomain else {
                throw error
            }

            switch transactionError.code {
            case Self.friendshipNotFoundCode:
                throw FriendshipRepositoryError.friendshipNotFound
            case Self.invalidFriendshipDataCode:
                throw FriendshipRepositoryError.invalidFriendshipData
            case Self.cannotRespondCode:
                throw FriendshipRepositoryError.cannotRespondToFriendRequest
            default:
                throw error
            }
        }
    }
}
