//
//  Friendship.swift
//  Caprichometro
//
//  Created by Danii on 25/10/2026.
//

import Foundation

enum FriendshipStatus: String {
    case pending
    case accepted
    case declined
}


struct Friendship: Equatable, Identifiable {
    let id: String
    let memberIDs: [String]
    let requestedBy: String
    let status: FriendshipStatus
    let createdAt: Date
    let updatedAt: Date
}

enum FriendRequestDecision {
    case accept
    case decline

    var status: FriendshipStatus {
        switch self {
        case .accept: .accepted
        case .decline: .declined
        }
    }
}
