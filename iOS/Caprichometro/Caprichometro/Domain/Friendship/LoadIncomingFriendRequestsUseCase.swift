//
//  LoadIncomingFriendRequestsUseCase.swift
//  Caprichometro
//
//  Created by Danii on 25/10/2026.
//

import Foundation

struct IncomingFriendRequest: Identifiable {
    let friendshipID: String
    let sender: UserProfile
    let createdAt: Date

    var id: String { friendshipID }
}

enum LoadIncomingFriendRequestsError: Error {
    case missingUserID
    case senderProfileNotFound
    case friendProfileNotFound
}

@MainActor
struct LoadIncomingFriendRequestsUseCase {
    private let friendshipRepository: any FriendshipRepository
    private let userProfileRepository: any UserProfileRepository

    init(
        friendshipRepository: any FriendshipRepository,
        userProfileRepository: any UserProfileRepository
    ) {
        self.friendshipRepository = friendshipRepository
        self.userProfileRepository = userProfileRepository
    }

    func execute(for userID: String) async throws -> [IncomingFriendRequest] {
        guard !userID.isEmpty else {
            throw LoadIncomingFriendRequestsError.missingUserID
        }

        let friendships = try await friendshipRepository
            .incomingRequests(for: userID)

        var incomingRequests: [IncomingFriendRequest] = []

        for friendship in friendships {
            guard let sender = try await userProfileRepository
                .findByID(friendship.requestedBy) else {
                throw LoadIncomingFriendRequestsError.senderProfileNotFound
            }

            incomingRequests.append(
                IncomingFriendRequest(
                    friendshipID: friendship.id,
                    sender: sender,
                    createdAt: friendship.createdAt
                )
            )
        }

        return incomingRequests.sorted {
            $0.createdAt > $1.createdAt
        }
    }

    func friends(for userID: String) async throws -> [UserProfile] {
        guard !userID.isEmpty else {
            throw LoadIncomingFriendRequestsError.missingUserID
        }

        let friendships = try await friendshipRepository
            .acceptedFriendships(for: userID)

        var friends: [UserProfile] = []

        for friendship in friendships {
            guard
                let friendID = friendship.memberIDs.first(where: {
                    $0 != userID
                }),
                let friend = try await userProfileRepository.findByID(friendID)
            else {
                throw LoadIncomingFriendRequestsError.friendProfileNotFound
            }

            friends.append(friend)
        }

        return friends.sorted {
            $0.username.localizedCaseInsensitiveCompare($1.username)
                == .orderedAscending
        }
    }
}
