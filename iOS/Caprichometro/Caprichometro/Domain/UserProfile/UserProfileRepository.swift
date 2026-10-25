//
//  UserProfileRepository.swift
//  Caprichometro
//
//  Created by Danii on 25/10/2026.
//

enum UserProfileRepositoryError: Error {
    case usernameAlreadyTaken
}
@MainActor
protocol UserProfileRepository: AnyObject {
    func isUsernameAvailable(_ username: String) async throws -> Bool
    func save(_ profile: UserProfile) async throws
}
