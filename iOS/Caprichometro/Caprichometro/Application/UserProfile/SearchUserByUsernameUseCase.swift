//
//  SearchUserByUsernameUseCase.swift
//  Caprichometro
//
//  Created by Danii on 25/10/2026.
//

import Foundation

enum SearchUserByUsernameError: Error {
    case invalidUsername
}

@MainActor
struct SearchUserByUsernameUseCase {
    private let userProfileRepository: any UserProfileRepository

    init(userProfileRepository: any UserProfileRepository) {
        self.userProfileRepository = userProfileRepository
    }

    func execute(username: String) async throws -> UserProfile? {
        let normalizedUsername = UsernameRules.normalize(username)

        guard UsernameRules.isValid(normalizedUsername) else {
            throw SearchUserByUsernameError.invalidUsername
        }

        return try await userProfileRepository
            .findByUsername(normalizedUsername)
    }
}
