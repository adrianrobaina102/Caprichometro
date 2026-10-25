//
//  SendFriendRequestUseCase.swift
//  Caprichometro
//
//  Created by Danii on 25/10/2026.
//

import Foundation

enum SendFriendRequestError: Error {
    case missingUserID
    case cannotRequestYourself
}

@MainActor
struct SendFriendRequestUseCase {
    private let friendshipRepository: any FriendshipRepository

    init(friendshipRepository: any FriendshipRepository) {
        self.friendshipRepository = friendshipRepository
    }

    func execute(
        requesterID: String,
        recipientID: String
    ) async throws {
        guard !requesterID.isEmpty, !recipientID.isEmpty else {
            throw SendFriendRequestError.missingUserID
        }

        guard requesterID != recipientID else {
            throw SendFriendRequestError.cannotRequestYourself
        }

        try await friendshipRepository.sendRequest(
            from: requesterID,
            to: recipientID
        )
    }
}
