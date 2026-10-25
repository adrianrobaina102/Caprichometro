//
//  RespondToFriendRequestUseCase.swift
//  Caprichometro
//
//  Created by Danii on 25/10/2026.
//

import Foundation

enum RespondToFriendRequestError: Error {
    case missingID
}

@MainActor
struct RespondToFriendRequestUseCase {
    private let friendshipRepository: any FriendshipRepository

    init(friendshipRepository: any FriendshipRepository) {
        self.friendshipRepository = friendshipRepository
    }

    func execute(
        friendshipID: String,
        userID: String,
        decision: FriendRequestDecision
    ) async throws {
        guard !friendshipID.isEmpty, !userID.isEmpty else {
            throw RespondToFriendRequestError.missingID
        }

        try await friendshipRepository.respond(
            to: friendshipID,
            by: userID,
            with: decision
        )
    }
}
