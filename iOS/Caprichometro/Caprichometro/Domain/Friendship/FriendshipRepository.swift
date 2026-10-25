//
//  FriendshipRepository.swift
//  Caprichometro
//
//  Created by Danii on 25/10/2026.
//
enum FriendshipRepositoryError: Error {
    case relationshipAlreadyExists
    case invalidFriendshipData
    case friendshipNotFound
    case cannotRespondToFriendRequest
}

@MainActor
protocol FriendshipRepository: AnyObject {
    func sendRequest(
        from requesterID: String,
        to recipientID: String
    ) async throws

    func incomingRequests(for userID: String) async throws -> [Friendship]
    func acceptedFriendships(for userID: String) async throws -> [Friendship]
    func respond(
        to friendshipID: String,
        by userID: String,
        with decision: FriendRequestDecision
    ) async throws
}
